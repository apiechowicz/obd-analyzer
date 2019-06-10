package com.example.obdanalyzer.obd2

import java.io.OutputStream

fun OutputStream.write(command: Obd2Command) = write(command.commandValue)

interface Obd2Command {
    val name: String
    val commandPrefix: String
    val commandCode: String
    val commandString get() = commandPrefix + commandCode
    val commandValue get() = (commandString + '\r').toByteArray()
}

enum class AtCommand(override val commandCode: String) : Obd2Command {
    RESTART("Z"),
    SET_ALL_DEFAULTS("D"),
    ECHO_OFF("E0"),
    SPACES_OFF("S0");

    override val commandPrefix: String
        get() = "AT"
}

enum class ServicePid(val pidValue: String) {
    CURRENT_DATA_SERVICE_PID("01");
}

enum class CurrentDataCommand(override val commandCode: String) : Obd2Command {
    ENGINE_LOAD("04"),
    ENGINE_RPM("0C"),
    VEHICLE_SPEED("0D");

    override val commandPrefix: String
        get() = ServicePid.CURRENT_DATA_SERVICE_PID.pidValue

    val responseCommandString: String
        get() = commandString.first() + 4 + commandString.substring(1)
}
