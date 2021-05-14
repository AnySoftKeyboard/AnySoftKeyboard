package com.anysoftkeyboard.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import java.io.File;
import net.evendanan.pixel.RxProgressDialog;

public class FileExplorerRestore extends AppCompatActivity {
    private ListView mListViewFiles;
    private File mBasePath;
    private File mCurrentFolder;

    private Disposable launch_restore(String fileName) {
        return RxProgressDialog.create(
                        new Pair<>(MainFragment.supportedProviders, MainFragment.checked),
                        this,
                        getText(R.string.take_a_while_progress_message),
                        R.layout.progress_window)
                .subscribeOn(RxSchedulers.background())
                .flatMap(GlobalPrefsBackup::restore)
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
                                                        + fileName,
                                                Toast.LENGTH_LONG)
                                        .show());
    }

    public void create_builder(File fileOutput) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.file_explorer_alert_title)
                .setMessage(R.string.file_explorer_restore_alert_message)
                .setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GlobalPrefsBackup.updateCustomFilename(fileOutput);
                                launch_restore(fileOutput.toString());
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void listFile(File basePath) {
        File[] files = basePath.listFiles();
        ArrayAdapter<File> adapter =
                new ArrayAdapter<File>(this, R.layout.file_explorer_single_item, files);
        mListViewFiles.setAdapter(adapter);

        // Set onclickListener for all element of listView
        mListViewFiles.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        Object o = mListViewFiles.getItemAtPosition(position);
                        if (new File(o.toString()).isDirectory()) {
                            mCurrentFolder = new File(o.toString());
                            setTitle(o.toString());
                            listFile(mCurrentFolder);
                        } else if (new File(o.toString()).isFile())
                            create_builder(new File(o.toString()));
                    }
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

        mListViewFiles = (ListView) findViewById(R.id.file_explorer_list_view);

        mBasePath = Environment.getExternalStorageDirectory();

        mCurrentFolder = mBasePath;

        setTitle(mBasePath.toString());

        listFile(mBasePath);
    }
}
