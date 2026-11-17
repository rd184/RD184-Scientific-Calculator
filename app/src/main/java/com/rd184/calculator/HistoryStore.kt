package com.rd184.calculator

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class HistoryStore(context: Context) : SQLiteOpenHelper(context, "rd184_history.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) { db.execSQL("CREATE TABLE history(id INTEGER PRIMARY KEY AUTOINCREMENT, mode TEXT NOT NULL, expression TEXT NOT NULL, result TEXT NOT NULL, created_at INTEGER NOT NULL)") }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { }
    fun add(mode:String, expression:String, result:String) { val v=ContentValues().apply { put("mode",mode);put("expression",expression);put("result",result);put("created_at",System.currentTimeMillis()) }; writableDatabase.insert("history",null,v) }
    fun all():List<Triple<String,String,String>> { val out=mutableListOf<Triple<String,String,String>>(); readableDatabase.rawQuery("SELECT mode, expression, result FROM history ORDER BY id DESC LIMIT 300",null).use { c->while(c.moveToNext())out+=Triple(c.getString(0),c.getString(1),c.getString(2)) };return out }
    fun clear() { writableDatabase.delete("history",null,null) }
}
