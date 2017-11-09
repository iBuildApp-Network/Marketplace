package com.appbuilder.core;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ListView;

import com.appbuilder.sdk.android.BaseActivity;
import com.appbuilder.sdk.android.MyContact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: macbookpro
 * Date: 01.12.14
 * Time: 14:44
 * To change this template use File | Settings | File Templates.
 */
public class ContactChooser extends BaseActivity {

    private ListView list;
    private List<MyContact> resultCont = new ArrayList<>();
    private com.appbuilder.core.SearchView1 search;
    private ContactAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showProgressDialog(getString(R.string.loading));
        new Thread( new Runnable() {
            @Override
            public void run() {
                initializeBackend();
            }
        }).start();

        initializeUI();
    }

    private void initializeUI() {
        setContentView(R.layout.masterapp_contact_chooser1);

        setTopbarTitle(getString(R.string.sharing_sms_title));
        setBackBtnText(getString(R.string.cancel));
        setBackBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setEditBtnText(getString(R.string.done));
        setEditBtnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
                Intent res = new Intent();
                List<MyContact> result = adapter.getCheckedList();
                res.putExtra("list", (Serializable) result);
                setResult(RESULT_OK, res);
                finish();
            }
        });

        search = (com.appbuilder.core.SearchView1) findViewById(R.id.editText1);
        search.setWatcher(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.getFilter().filter(s.toString());
            }
        });

        list = (ListView) findViewById(R.id.listView);
        list.setDivider(null);
        list.setCacheColorHint(Color.TRANSPARENT);
    }

    private void initializeBackend()
    {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        List<MyContact> contacts = new ArrayList<MyContact>();

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                MyContact tempContact = new MyContact();
                tempContact.id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                tempContact.name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                            new String[]{tempContact.id}, null);
                    while ( pCur.moveToNext() ) {
                        tempContact.phones.add(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                    pCur.close();
                }

                contacts.add(tempContact);
            }
        }

        resultCont = new ArrayList<MyContact>();
        for ( MyContact c : contacts )
        {
            if ( c.phones.size() > 0 )
            {
                for (int i = 0; i < c.phones.size() ; i++) {
                    List<String> phonelist = new ArrayList<String>();
                    phonelist.add(c.phones.get(i));
                    MyContact newContact = new MyContact( c.id+"."+i, c.name, phonelist, false);
                    resultCont.add(newContact);
                }
            }
        }

        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                hideProgressDialog();
                adapter = new ContactAdapter(ContactChooser.this, resultCont);
                list.setAdapter(adapter);
            }
        });
    }

}

