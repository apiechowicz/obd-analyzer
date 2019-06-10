package com.example.obdanalyzer.obd2

import org.slf4j.LoggerFactory

object Obd2ResponseParser {
    private val LOG = LoggerFactory.getLogger(Obd2ResponseParser::class.java)
    private const val BUS_OK: String = "BUSINIT:OK"
    private const val BUS_ERROR: String = "BUSINIT:ERROR"
    const val NO_DATA: String = "NODATA"

    fun parseBusInitCommand(response: String, command: Obd2Command, commandEcho: Boolean): String {
        val parsedResponse = removeEcho(response, commandEcho, command)
        if (parsedResponse.startsWith(BUS_OK)) {
            LOG.info(BUS_OK)
            return parseCommandResponse(parsedResponse.replaceFirst(BUS_OK, ""), command, false)
        } else if (parsedResponse.startsWith(BUS_ERROR)) {
            LOG.error(BUS_ERROR)
            return Obd2Connection.ERROR
        }
        error("Response is not a bus initialization response: '$parsedResponse'")
    }

    private fun removeEcho(response: String, commandEcho: Boolean, command: Obd2Command): String =
        if (commandEcho) response.replaceFirst(command.commandString, "") else response

    fun parseCommandResponse(response: String, command: Obd2Command, commandEcho: Boolean): String {
        val parsedResponse = removeEcho(response, commandEcho, command)
        when (command) {
            is AtCommand -> return parsedResponse
            is CurrentDataCommand -> {
                val hexValue = parsedResponse.replaceFirst(command.responseCommandString, "")
                if (hexValue == NO_DATA) {
                    return NO_DATA
                }
                var intValue = hexValue.toInt(radix = 16)
                if (command == CurrentDataCommand.ENGINE_RPM) {
                    intValue /= 4
                }
                return intValue.toString()
            }
            else -> error("Unrecognized command type: $command")
        }
    }
}
