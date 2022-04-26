package com.x.a_technologies.dictionary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.x.a_technologies.dictionary.adapter.SaveAdapter
import com.x.a_technologies.dictionary.databinding.FragmentLastSearchedBinding
import com.x.a_technologies.dictionary.room.AppDatabase
import com.x.a_technologies.dictionary.room.MyWordDb

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class LastSearchedFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var binding: FragmentLastSearchedBinding
    lateinit var appDatabase: AppDatabase
    lateinit var list: ArrayList<MyWordDb>
    lateinit var saveAdapter: SaveAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLastSearchedBinding.inflate(
            LayoutInflater.from(requireContext()),
            container,
            false
        )




        return binding.root
    }

    override fun onResume() {
        super.onResume()
        appDatabase = AppDatabase.getInstance(context)
        list = ArrayList()
        list.addAll(appDatabase.wordDao().getAllMyWordDb())

        saveAdapter = SaveAdapter(list, object : SaveAdapter.RvClick {
            override fun saveClick(myWordDb: MyWordDb, position: Int) {
                if (myWordDb.save){
                    myWordDb.save = false
                    Toast.makeText(context, "${myWordDb.word} olib tashlandi", Toast.LENGTH_SHORT)
                        .show()
                }else{
                    myWordDb.save = true
                    Toast.makeText(context, "${myWordDb.word} saqlandi", Toast.LENGTH_SHORT)
                        .show()
                }


                appDatabase.wordDao().editWord(myWordDb)
                saveAdapter.notifyItemChanged(position)

            }
        })
        binding.rvSave.adapter = saveAdapter
        if (list.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
        } else {
            binding.tvEmpty.visibility = View.GONE
        }
    }
}