package com.anysoftkeyboard.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.menny.android.anysoftkeyboard.R;
import io.reactivex.disposables.Disposable;
import java.io.File;
import net.evendanan.pixel.RxProgressDialog;

public class FileExplorerCreate extends AppCompatActivity {
    private ListView listViewFiles;
    private File currentFolder;
    private File basePath;

    public void listFile(File basePath) {
        File[] files = basePath.listFiles();
        ArrayAdapter<File> adapter =
                new ArrayAdapter<File>(this, R.layout.file_explorer_single_item, files);
        listViewFiles.setAdapter(adapter);

        // Set onclickListener for all element of listView
        listViewFiles.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> parent, View view, int position, long id) {
                        Object o = listViewFiles.getItemAtPosition(position);
                        if (new File(o.toString()).isDirectory()) {
                            currentFolder = new File(o.toString());
                            setTitle(o.toString());
                            listFile(currentFolder);
                        } else if (new File(o.toString()).isFile())
                            create_builder(new File(o.toString()));
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (!currentFolder.equals(basePath)) {
            int sep = currentFolder.toString().lastIndexOf("/");
            setTitle(currentFolder.toString().substring(0, sep));
            currentFolder = new File(currentFolder.toString().substring(0, sep));
            listFile(currentFolder);
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
                new File(currentFolder.toString() + "/askBackup").mkdir();
                Toast.makeText(
                                getApplicationContext(),
                                "Folder askBackup has been created at " + currentFolder.toString(),
                                Toast.LENGTH_LONG)
                        .show();
                listFile(currentFolder);
                return true;
            case R.id.file_explorer_menu_refresh:
                listFile(currentFolder);
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_create_main_ui);

        TextView filenameTextView = (TextView) findViewById(R.id.file_explorer_filename);
        ImageButton filenameButton = (ImageButton) findViewById(R.id.file_explorer_filename_button);
        listViewFiles = (ListView) findViewById(R.id.file_explorer_list_view);

        basePath = Environment.getExternalStorageDirectory();

        currentFolder = basePath;

        setTitle(basePath.toString());

        listFile(basePath);

        filenameButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (filenameTextView.length() > 0) {
                            final File fileOutput =
                                    new File(
                                            currentFolder
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
