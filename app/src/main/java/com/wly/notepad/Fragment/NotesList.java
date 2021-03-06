package com.wly.notepad.Fragment;


import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.com.wly.notepad.R;
import com.wly.notepad.Activity.AddNotesActivity;
import com.wly.notepad.Activity.CheckNoteActivity;
import com.wly.notepad.Adapter.NoteAdapter;
import com.wly.notepad.Manager.NotesDB;

import com.wly.notepad.Model.MainUser;

import com.wly.notepad.Util.MapUtils;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wly.notepad.Util.NoteItem;

import java.util.LinkedList;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotesList extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher, AdapterView.OnItemLongClickListener {

    private FloatingActionButton btnAdd;
    private ListView lv;
    private EditText etSearch;
    private Intent intent;
    private NotesDB notesDB;
    private NoteAdapter adapter;
    private SQLiteDatabase dbReader;
    private Cursor cursor;
    private LinkedList<NoteItem> noteLists= new LinkedList<NoteItem>();
    public NotesList() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.activity_noteslist,null);
        initView(view);
        initListener();
        return view;
    }

    public void initView(View view)
    {
        //获取兼容低版本的ActionBar
        Toolbar toolbar = (Toolbar)view.findViewById(R.id.notelist_toolbar);
        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        btnAdd =(FloatingActionButton)view.findViewById(R.id.addNotes);
        lv = (ListView) view.findViewById(R.id.notes_list);
        etSearch = (EditText) view.findViewById(R.id.edit_search);

        //打开数据库
        notesDB = new NotesDB(getContext(),"notes.db",null,1);
        dbReader = notesDB.getWritableDatabase();
        //初始化列表项
        queryNotesAll();
        //设置适配器
        adapter = new NoteAdapter(getContext(),noteLists);
        lv.setAdapter(adapter);
    }

    public void initListener()
    {
        btnAdd.setOnClickListener(this);
        lv.setOnItemClickListener(this);
        lv.setOnItemLongClickListener(this);
        etSearch.addTextChangedListener(this);
    }
    //查询表中的所有信息
    private void queryNotesAll() {
        cursor = dbReader.rawQuery("select * from notes where user_id='"+ MainUser.user.getId()+"'",null);
    }
    //查询表中的部分信息
    private void queryData(String searchData)
    {
        cursor = dbReader.rawQuery("select * from notes where notes_content like '%" + searchData + "%' or notes_tag like '%" + searchData + "%'", null);
    }
    //删除列表项
    private void deleteData(int id)
    {
        String ID = String.valueOf(id);
        dbReader.execSQL("delete from notes where notes_id=?",new String[]{ID});
    }
    @Override
    public void onResume() {
        super.onResume();
        //刷新数据库信息
        queryNotesAll();
        refreshListView();

    }
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.addNotes:
                startActivity(new Intent(getActivity(), AddNotesActivity.class));
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        cursor.moveToPosition(position);    //获取到数据库中当前一行
        Intent i = new Intent(getActivity(),CheckNoteActivity.class);
        //传递当前行的数据
        i.putExtra(NotesDB.NOTES_ID,
                cursor.getInt(cursor.getColumnIndex(NotesDB.NOTES_ID)));
        i.putExtra(NotesDB.NOTES_CONTENT,
                cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_CONTENT)));
        i.putExtra(NotesDB.NOTES_TAG,
                cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_TAG)));
        i.putExtra(NotesDB.NOTES_TIME,
                cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_TIME)));
        i.putExtra(NotesDB.NOTES_PIC,
                cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_PIC)));
        i.putExtra(NotesDB.NOTES_VIDEO,
                cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_VIDEO)));
        i.putExtra(NotesDB.NOTES_GRAFIITI,
                cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_GRAFIITI)));
        startActivity(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cursor.close();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    /**
     * EditText搜索框对输入值变化的监听，实时搜索
     */
    // TODO: 2019/6/4 3、使用TextWatcher实现对实时搜索
    @Override
    public void afterTextChanged(Editable s) {
        if (etSearch.getText().toString().equals("")) {
            queryNotesAll();
        } else {
            String searchString = etSearch.getText().toString();
            queryData(searchString);
        }
        refreshListView();
    }
    private void refreshListView()
    {
        noteLists.clear();
        while (cursor.moveToNext()) {
            String content = cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_CONTENT));
            String time = cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_TIME));
            String image = cursor.getString(cursor.getColumnIndex(NotesDB.NOTES_TAG));
            int imageId = MapUtils.imageMap.get(image);
            NoteItem note = new NoteItem(content, time, imageId);
            noteLists.add(note);
        }
        adapter = new NoteAdapter(getContext(),noteLists);
        lv.setAdapter(adapter);
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, View view,final int position, long id) {
        AlertDialog alert = null;
        AlertDialog.Builder builder  = new AlertDialog.Builder(getContext());
        alert = builder.setTitle("温馨提示：")
                .setMessage("确定删除吗？")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getContext(), "你点击了取消按钮~", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*
                         *
                         */
                        cursor.moveToPosition(position);    //获取到数据库中当前一行
                        int id = cursor.getInt(cursor.getColumnIndex(NotesDB.NOTES_ID));
                        deleteData(id);
                        noteLists.remove(position);
                        adapter.notifyDataSetChanged();
                    }
                }).create();
        alert.show();
        return true;


    }
}
