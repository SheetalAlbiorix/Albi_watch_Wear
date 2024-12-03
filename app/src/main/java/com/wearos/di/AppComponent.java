package com.wearos.di;

import com.wearos.activity.MainActivity;

import dagger.Component;

@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MainActivity activity);
}
