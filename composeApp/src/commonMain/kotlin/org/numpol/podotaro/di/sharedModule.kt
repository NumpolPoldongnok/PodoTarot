package org.numpol.podotaro.di


import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.numpol.podotaro.taro.presentation.main.TarotMainViewModel

val sharedModule = module {
//    single { HttpClientFactory.create(get()) }
//    singleOf(::KtorRemoteBookDataSource).bind<RemoteBookDataSource>()
//    singleOf(::DefaultBookRepository).bind<BookRepository>()
//
//    single {
//        get<DatabaseFactory>().create()
//            .setDriver(BundledSQLiteDriver())
//            .build()
//    }
//    single { get<FavoriteBookDatabase>().favoriteBookDao }
//
//    viewModelOf(::BookListViewModel)
//    viewModelOf(::BookDetailViewModel)
    viewModelOf(::TarotMainViewModel)
}