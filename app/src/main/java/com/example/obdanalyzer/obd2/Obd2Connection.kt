package com.example.obdanalyzer.obd2

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean

class Obd2Connection(
    private val output: OutputStream,
    private val input: InputStream,
    private val running: AtomicBoolean
) {
    private var commandEcho = true

    fun performInitializationSequence(): Boolean {
        if (!performRestart()) return false
        if (!performInitializationCommand(AtCommand.SET_ALL_DEFAULTS)) return false
        if (!performInitializationCommand(AtCommand.ECHO_OFF)) return false
        commandEcho = false
        if (!performInitializationCommand(AtCommand.SPACES_OFF)) return false
        return performBusInitialization()
    }

    private fun performRestart(): Boolean {
        val command = AtCommand.RESTART
        sendCommand(command)
        val parsedResponse = Obd2ResponseParser.parseCommandResponse(receiveResponse(input), command, commandEcho)
        if (parsedResponse == ERROR) {
            closeStreams()
            return false
        }
        logCommandResponse(command, parsedResponse)
        return true
    }

    private fun sendCommand(command: Obd2Command) {
        output.write(command)
        LOG.info("Sent command: ${command.name}")
    }

    private fun receiveResponse(input: InputStream): String {
        return buildString {
            responseBuilder@ while (true) {
                val size = input.available()
                when {
                    size < 0 -> return ERROR
                    size == 0 -> Thread.sleep(DATA_AWAIT_TIMEOUT_MILLIS)
                    size > 0 -> when (val char = input.read()) {
                        62 -> break@responseBuilder // value 62 represents '>' which is used to indicate the end of response
                        in 33..126 -> append(char.toChar()) // values between 33 and 127 are non empty characters in ascii
                    }
                }
            }
        }
    }

    private fun closeStreams() {
        output.close()
        input.close()
    }

    private fun logCommandResponse(command: Obd2Command, response: String) =
        LOG.info("Response for ${command.name}: '$response'")

    private fun performInitializationCommand(command: Obd2Command): Boolean {
        sendCommand(command)
        val parsedResponse = Obd2ResponseParser.parseCommandResponse(receiveResponse(input), command, commandEcho)
        if (parsedResponse != OK) {
            closeStreams()
            return false
        }
        logCommandResponse(command, parsedResponse)
        return true
    }

    private fun performBusInitialization(): Boolean {
        val command = CurrentDataCommand.ENGINE_RPM
        sendCommand(command)
        val response = receiveResponse(input)
        if (response == ERROR) {
            closeStreams()
            return false
        }
        val busInitResult = Obd2ResponseParser.parseBusInitCommand(response, command, commandEcho)
        if (busInitResult == ERROR) {
            closeStreams()
            return false
        }
        return true
    }

    fun transmitData() {
        connectionLoop@ while (running.get()) {
            for (command in CurrentDataCommand.values()) {
                sendCommand(command)
                val response = receiveResponse(input)
                if (response == ERROR) {
                    LOG.error("Input stream returned negative number of available bytes")
                    break@connectionLoop
                }
                val parsedResponse = Obd2ResponseParser.parseCommandResponse(response, command, commandEcho)
                LOG.info("Response for ${command.name}: '$parsedResponse'")
                DataProvider.sendData(Obd2Data(command, parsedResponse))
            }
            Thread.sleep(REQUEST_INTERVAL_MILLIS)
        }
        closeStreams()
    }

    companion object Obd2Connection {
        private val LOG: Logger = LoggerFactory.getLogger(Obd2Connection::class.java)
        private const val DATA_AWAIT_TIMEOUT_MILLIS: Long = 100
        private const val REQUEST_INTERVAL_MILLIS: Long = 1_000
        const val ERROR: String = "ERROR"
        private const val OK: String = "OK"
    }
}
