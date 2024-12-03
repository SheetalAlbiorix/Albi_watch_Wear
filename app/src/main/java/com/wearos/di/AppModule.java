package com.wearos.di;

import com.wearos.model.AuthRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {
    @Provides
    public AuthRepository provideWatchRepository() {
        return new AuthRepository();
    }
}

