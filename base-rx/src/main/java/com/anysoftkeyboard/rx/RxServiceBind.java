package com.anysoftkeyboard.rx;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class RxServiceBind {
    public static <B extends Binder> Observable<B> bind(
            final Context context, final Intent launch) {
        return bind(context, launch, Service.BIND_AUTO_CREATE);
    }

    public static <B extends Binder> Observable<B> bind(
            final Context context, final Intent launch, final int flags) {
        return Observable.using(
                Connection::new,
                (final Connection<B> con) -> {
                    context.getApplicationContext().bindService(launch, con, flags);
                    return Observable.create(con);
                },
                context::unbindService);
    }

    private static class Connection<B extends Binder>
            implements ServiceConnection, ObservableOnSubscribe<B> {
        private ObservableEmitter<? super B> mSubscriber;

        @Override
        @SuppressWarnings("unchecked")
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (mSubscriber != null && service != null) {
                mSubscriber.onNext((B) service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (mSubscriber != null) {
                mSubscriber.onComplete();
            }
        }

        @Override
        public void subscribe(ObservableEmitter<B> observableEmitter) throws Exception {
            mSubscriber = observableEmitter;
        }
    }
}
