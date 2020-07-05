package com.anysoftkeyboard.ui;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import net.evendanan.pixel.RxProgressDialog;

import java.io.File;
import io.reactivex.disposables.Disposable;

public class FileExplorerCreate extends AppCompatActivity {
    private ListView listViewFiles;
    private File currentFolder;
    private File basePath;

    public void listFile(File basePath)
    {
        File[] files = basePath.listFiles();
        ArrayAdapter<File> adapter = new ArrayAdapter<File>(this,
                R.layout.file_explorer_single_item, files);
        listViewFiles.setAdapter(adapter);

        //Set onclickListener for all element of listView
        listViewFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = listViewFiles.getItemAtPosition(position);
                if (new File(o.toString()).isDirectory()) {
                    currentFolder = new File(o.toString());
                    setTitle(o.toString());
                    listFile(currentFolder);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (!currentFolder.equals(basePath))
        {
            int sep = currentFolder.toString().lastIndexOf("/");
            setTitle(currentFolder.toString().substring(0, sep));
            listFile(new File(currentFolder.toString().substring(0, sep)));
        }
        else
            finish();
    }

    public void emptyFilenameError()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.file_explorer_filename_empty);
        alertDialogBuilder.setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = alertDialogBuilder.create();
        dialog.show();
    }

    public Disposable launch_backup(File fileOutput)
    {
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
                                        "MainFragment",
                                        "Finished backing up %s",
                                        providerDetails.provider.providerId()),
                        e -> {
                            Logger.w(
                                    "MainFragment",
                                    e,
                                    "Failed to do operation due to %s",
                                    e.getMessage());
                            Toast.makeText(getApplicationContext(), "Your data have failed to be saved", Toast.LENGTH_LONG).show();
                        },
                        () ->
                                Toast.makeText(getApplicationContext(), "Your data have been saved to " + fileOutput.toString(), Toast.LENGTH_LONG).show());
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

        filenameButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (filenameTextView.length() > 0)
                        {
                            final File fileOutput = new File(currentFolder + "/" + filenameTextView.getText().toString() + ".xml");

                            GlobalPrefsBackup.updateCustomFilename(fileOutput);
                            launch_backup(fileOutput);

                            finish();
                        }
                        else
                            emptyFilenameError();
                    }
        });
    }
}
