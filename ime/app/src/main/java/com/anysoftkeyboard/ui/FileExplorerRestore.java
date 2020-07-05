package com.anysoftkeyboard.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.anysoftkeyboard.base.utils.Logger;
import com.anysoftkeyboard.prefs.GlobalPrefsBackup;
import com.anysoftkeyboard.rx.RxSchedulers;
import com.anysoftkeyboard.ui.settings.MainFragment;
import com.menny.android.anysoftkeyboard.R;

import net.evendanan.pixel.RxProgressDialog;

import java.io.File;

import io.reactivex.disposables.Disposable;

public class FileExplorerRestore extends AppCompatActivity {
    private ListView listViewFiles;
    private File basePath;
    private File currentFolder;

    private Disposable launch_restore() {
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
                                        "MainFragment",
                                        "Finished restore up %s",
                                        providerDetails.provider.providerId()),
                        e -> {
                            Logger.w(
                                    "MainFragment",
                                    e,
                                    "Failed to do operation due to %s",
                                    e.getMessage());
                            Toast.makeText(getApplicationContext(), "Your data have failed to be restored", Toast.LENGTH_LONG).show();
                        },
                        () ->
                                Toast.makeText(getApplicationContext(), "Your data have been restored", Toast.LENGTH_LONG).show());
    }

    public void create_builder(){
        new AlertDialog.Builder(this)
                .setTitle("Select this file ?")
                .setMessage("Do you want to restore this file ?")

                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        launch_restore();
                        finish();
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

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
                else if (new File(o.toString()).isFile())
                    create_builder();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_explorer_restore_main_ui);

        listViewFiles = (ListView) findViewById(R.id.file_explorer_list_view);

        basePath = Environment.getExternalStorageDirectory();

        currentFolder = basePath;

        setTitle(basePath.toString());

        listFile(basePath);
    }

}
