package com.anysoftkeyboard.ui;

import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import java.io.File;
import net.evendanan.pixel.RxProgressDialog;

public class FileExplorerRestore extends AppCompatActivity {
    private final CompositeDisposable mActionsDisposables = new CompositeDisposable();
    private ListView mListViewFiles;
    private File mBasePath;
    private File mCurrentFolder;

    private Disposable launchRestore(@NonNull File file) {
        return RxProgressDialog.create(
                        new Pair<>(MainFragment.supportedProviders, MainFragment.checked),
                        this,
                        getText(R.string.take_a_while_progress_message),
                        R.layout.progress_window)
                .subscribeOn(RxSchedulers.background())
                .flatMap(p -> GlobalPrefsBackup.restore(p, file))
                .observeOn(RxSchedulers.mainThread())
                .subscribe(
                        providerDetails ->
                                Logger.i(
                                        "FileExplorerRestore",
                                        "Finished restore up %s",
                                        providerDetails.provider.providerId()),
                        e -> {
                            Logger.w(
                                    "FileExplorerRestore",
                                    e,
                                    "Failed to do operation due to %s",
                                    e.getMessage());
                            Toast.makeText(
                                            getApplicationContext(),
                                            this.getString(R.string.file_explorer_restore_failed),
                                            Toast.LENGTH_LONG)
                                    .show();
                        },
                        () ->
                                Toast.makeText(
                                                getApplicationContext(),
                                                this.getString(
                                                                R.string
                                                                        .file_explorer_restore_success)
                                                        + file,
                                                Toast.LENGTH_LONG)
                                        .show());
    }

    public void createBuilder(File fileOutput) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.file_explorer_alert_title)
                .setMessage(R.string.file_explorer_restore_alert_message)
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> {
                            mActionsDisposables.add(launchRestore(fileOutput));
                            finish();
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mActionsDisposables.dispose();
    }

    public void listFile(File basePath) {
        File[] files = basePath.listFiles();
        ArrayAdapter<File> adapter =
                new ArrayAdapter<>(this, R.layout.file_explorer_single_item, files);
        mListViewFiles.setAdapter(adapter);

        // Set onclickListener for all element of listView
        mListViewFiles.setOnItemClickListener(
                (parent, view, position, id) -> {
                    Object o = mListViewFiles.getItemAtPosition(position);
                    if (new File(o.toString()).isDirectory()) {
                        mCurrentFolder = new File(o.toString());
                        setTitle(o.toString());
                        listFile(mCurrentFolder);
                    } else if (new File(o.toString()).isFile())
                        createBuilder(new File(o.toString()));
                });
    }

    @Override
    public void onBackPressed() {
        if (!mCurrentFolder.equals(mBasePath)) {
            int sep = mCurrentFolder.toString().lastIndexOf("/");
            setTitle(mCurrentFolder.toString().substring(0, sep));
            mCurrentFolder = new File(mCurrentFolder.toString().substring(0, sep));
            listFile(mCurrentFolder);
        } else finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_restore_main_ui);

        mListViewFiles = findViewById(R.id.file_explorer_list_view);

        mBasePath = Environment.getExternalStorageDirectory();

        mCurrentFolder = mBasePath;

        setTitle(mBasePath.toString());

        listFile(mBasePath);
    }
}
