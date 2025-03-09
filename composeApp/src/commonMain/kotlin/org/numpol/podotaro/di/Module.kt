package org.numpol.podotaro.di


import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.numpol.podotaro.taro.data.database.DatabaseFactory
import org.numpol.podotaro.taro.data.database.FortuneRecordDatabase
import org.numpol.podotaro.taro.data.repository.DefaultFortuneRecordRepository
import org.numpol.podotaro.taro.domain.FortuneRecordRepository
import org.numpol.podotaro.taro.presentation.main.TarotMainViewModel

expect val platformModule: Module

val sharedModule = module {
//    single { HttpClientFactory.create(get()) }
//    singleOf(::KtorRemoteBookDataSource).bind<RemoteBookDataSource>()
    singleOf(::DefaultFortuneRecordRepository).bind<FortuneRecordRepository>()


//
    single {
        get<DatabaseFactory>().create()
            .setDriver(BundledSQLiteDriver())
            .build()
    }
    single { get<FortuneRecordDatabase>().fortuneRecordDao }
//
//    viewModelOf(::BookListViewModel)
//    viewModelOf(::BookDetailViewModel)
    viewModelOf(::TarotMainViewModel)
}