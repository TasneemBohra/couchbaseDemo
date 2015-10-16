package com.android.todocouchdb;
import android.app.*;
import android.app.AlertDialog;
import android.os.*;

import com.android.todocouchdb.adapter.TodoAdapter;
import com.couchbase.lite.*;
import com.couchbase.lite.android.*;
import android.util.*;
import java.util.*;
import com.shamanland.fab.*;
import android.support.v7.app.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.widget.AdapterView.OnItemLongClickListener;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "Couchbase";
    private Manager couchbaseManager;
    private Database couchbaseDatabase;
    private ListView todoList;
    private ArrayList<Document> todoArray;
    private TodoAdapter adapter;
    private FloatingActionButton fab;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.todoList = (ListView) findViewById(R.id.todo_list);
        this.todoArray = new ArrayList<Document>();
        this.adapter = new TodoAdapter(this, this.todoArray);
        this.todoList.setAdapter(this.adapter);

        this.fab = (FloatingActionButton) findViewById(R.id.fab);
        this.todoList.setOnTouchListener(new ShowHideOnScroll(this.fab));

        try {
            this.couchbaseManager = new Manager(new AndroidContext(this), Manager.DEFAULT_OPTIONS);
            this.couchbaseDatabase = this.couchbaseManager.getDatabase("mydb");
            Query allDocumentsQuery = couchbaseDatabase.createAllDocumentsQuery();
            QueryEnumerator queryResult = allDocumentsQuery.run();
            for (Iterator<QueryRow> it = queryResult; it.hasNext(); ) {
                QueryRow row = it.next();
                todoArray.add(row.getDocument());
                adapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Log.e(TAG, "An error happened", e);
            return;
        }

        if(this.couchbaseDatabase != null) {
            this.couchbaseDatabase.addChangeListener(new Database.ChangeListener() {
                public void changed(Database.ChangeEvent event) {
                    for(int i = 0; i < event.getChanges().size(); i++) {
                        Document retrievedDocument = couchbaseDatabase.getDocument(event.getChanges().get(i).getDocumentId());
                        if(retrievedDocument.isDeleted()) {
                            int documentIndex = adapter.getIndexOf(retrievedDocument.getId());
                            if(documentIndex > -1) {
                                todoArray.remove(documentIndex);
                            }
                        } else {
                            todoArray.add(retrievedDocument);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }

        this.todoList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> a, android.view.View v, int position, long id) {
                final Document listItemDocument = (Document) todoList.getItemAtPosition(position);
                final int listItemIndex = position;
                final CharSequence[] items = {"Delete Item"};
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Perform Action");
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            try {
                                listItemDocument.delete();
                            } catch (Exception e) {
                                Log.e(TAG, "An error happened", e);
                            }
                        }
                    }
                });
                AlertDialog alert = builder.show();
                return true;
            }
        });

    }

    public void addTodo(android.view.View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New TODO Item");
        final EditText input = new EditText(this);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Map<String, Object> docContent = new HashMap<String, Object>();
                docContent.put("todo", input.getText().toString());
                Document document = couchbaseDatabase.createDocument();
                try {
                    document.putProperties(docContent);
                } catch (CouchbaseLiteException e) {
                    Log.e(TAG, "Cannot write document to database", e);
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

}