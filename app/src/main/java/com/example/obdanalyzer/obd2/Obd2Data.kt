package com.example.obdanalyzer.obd2

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

data class Obd2Data(val command: Obd2Command, val value: String)

object DataProvider {
    private val subject = PublishSubject.create<Obd2Data>()

    fun sendData(data: Obd2Data) = subject.onNext(data)

    fun getDataObservable(): Observable<Obd2Data> = subject
}
