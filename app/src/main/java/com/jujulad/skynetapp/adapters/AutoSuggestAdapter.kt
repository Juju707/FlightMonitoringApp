package com.jujulad.skynetapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import java.util.*

class AutoSuggestAdapter(
    context: Context,
    private val resource: Int,
    private val items: List<String>
) : ArrayAdapter<Any?>(context, resource, 0, items) {
    private val tempItems: List<String>
    private val suggestions: MutableList<String>

    init {
        tempItems = ArrayList(items)
        suggestions = ArrayList()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(resource, parent, false)
        }
        val item = items[position]
        if (item != null && view is TextView) {
            view.text = item
        }
        return view!!
    }

    override fun getFilter(): Filter {
        return nameFilter
    }

    var nameFilter: Filter = object : Filter() {
        override fun convertResultToString(resultValue: Any): CharSequence {
            return resultValue as String
        }

        override fun performFiltering(constraint: CharSequence): FilterResults {
            return if (constraint != null) {
                suggestions.clear()
                for (names in tempItems) {
                    if (names.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(names)
                    }
                }
                val filterResults = FilterResults()
                filterResults.values = suggestions
                filterResults.count = suggestions.size
                println(filterResults)
                filterResults
            } else {
                FilterResults()
            }
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            var filterList: List<String> =
                results.values as ArrayList<String>
            if (results != null && results.count > 0) {
                clear()
                for (item in filterList) {
                    add(item)
                    notifyDataSetChanged()
                }
            }
        }
    }



}