package com.vessenger.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.vessenger.R

class ListViewAdapter (private val _data: ArrayList<HashMap<String, Any>>) : BaseAdapter() {
    override fun getCount(): Int {
        return _data.size
    }

    override fun getItem(_index: Int): HashMap<String, Any> {
        return _data[_index]
    }

    override fun getItemId(_index: Int): Long {
        return _index.toLong()
    }

    override fun getView(_position: Int, _v: View?, _container: ViewGroup?): View? {
        val _inflater = LayoutInflater.from(_container?.context)
        var _view = _v
        if (_view == null) {
            _view = _inflater.inflate(R.layout.activity_chat_list_view, _container, false)
        }

        val text2 = _view?.findViewById<TextView>(R.id.text2)

        if (text2 != null) {
            text2.text = _data[_position]["message"].toString()
        }

        return _view
    }

}
