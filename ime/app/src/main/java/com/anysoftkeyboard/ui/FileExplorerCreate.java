package com.anysoftkeyboard.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
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

public class FileExplorerCreate extends AppCompatActivity {
    private ListView mListViewFiles;
    private File mCurrentFolder;
    private File mBasePath;

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

    public void emptyFilenameError() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.file_explorer_filename_empty);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public Disposable launch_backup(String fileOutput) {
        return RxProgressDialog.create(
                        new Pair<>(MainFragment.supportedProviders, MainFragment.checked),
                        this,
                        getText(R.string.take_a_while_progress_message),
                        R.layout.progress_window)
                .subscribeOn(RxSchedulers.background())
                .flatMap(GlobalPrefsBackup::backup)
                .observeOn(RxSchedulers.mainThread())
                .subscribe(
                        providerDetails ->
                                Logger.i(
                                        "FileExplorerCreate",
                                        "Finished backing up %s",
                                        providerDetails.provider.providerId()),
                        e -> {
                            Logger.w(
                                    "FileExplorerCreate",
                                    e,
                                    "Failed to do operation due to %s",
                                    e.getMessage());
                            Toast.makeText(
                                            getApplicationContext(),
                                            this.getString(R.string.file_explorer_backup_failed),
                                            Toast.LENGTH_LONG)
                                    .show();
                        },
                        () ->
                                Toast.makeText(
                                                getApplicationContext(),
                                                this.getString(
                                                                R.string
                                                                        .file_explorer_backup_success)
                                                        + fileOutput,
                                                Toast.LENGTH_LONG)
                                        .show());
    }

    public void create_builder(File outputFile) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.file_explorer_alert_title)
                .setMessage(R.string.file_explorer_backup_alert_message)
                .setPositiveButton(
                        android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                launch_backup(outputFile.toString());
                                finish();
                            }
                        })
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_explorer_create_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.file_explorer_menu_add_folder:
                new File(mCurrentFolder.toString() + "/askBackup").mkdir();
                Toast.makeText(
                                getApplicationContext(),
                                "Folder askBackup has been created at " + mCurrentFolder.toString(),
                                Toast.LENGTH_LONG)
                        .show();
                listFile(mCurrentFolder);
                return true;
            case R.id.file_explorer_menu_refresh:
                listFile(mCurrentFolder);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_create_main_ui);

        TextView filenameTextView = (TextView) findViewById(R.id.file_explorer_filename);
        ImageButton filenameButton = (ImageButton) findViewById(R.id.file_explorer_filename_button);
        mListViewFiles = (ListView) findViewById(R.id.file_explorer_list_view);

        mBasePath = Environment.getExternalStorageDirectory();

        mCurrentFolder = mBasePath;

        setTitle(mBasePath.toString());

        listFile(mBasePath);

        filenameButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (filenameTextView.length() > 0) {
                            final File fileOutput =
                                    new File(
                                            mCurrentFolder
                                                    + "/"
                                                    + filenameTextView.getText().toString()
                                                    + ".xml");

                            GlobalPrefsBackup.updateCustomFilename(fileOutput);
                            if (fileOutput.exists()) create_builder(fileOutput);
                            else {
                                launch_backup(fileOutput.toString());
                                finish();
                            }
                        } else emptyFilenameError();
                    }
                });
    }
}
