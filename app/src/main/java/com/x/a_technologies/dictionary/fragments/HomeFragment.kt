package com.x.a_technologies.dictionary.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.x.a_technologies.dictionary.R
import com.x.a_technologies.dictionary.databinding.FragmentHomeBinding
import com.x.a_technologies.dictionary.models.MyWord
import com.x.a_technologies.dictionary.room.AppDatabase
import com.x.a_technologies.dictionary.room.MyWordDb
import com.x.a_technologies.dictionary.viewmodels.UserViewModel
import com.x.a_technologies.dictionary.viewmodels.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


class HomeFragment : Fragment(), MediaPlayer.OnPreparedListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var userViewModel: UserViewModel
    private val TAG = "HomeFragment"
    var myWord: MyWord? = null
    var myWordDb: MyWordDb? = null
    lateinit var appDatabase: AppDatabase
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        appDatabase = AppDatabase.getInstance(context)

        binding.tvDate.text = SimpleDateFormat("dd.MM.yyyy").format(Date())

        binding.edtSearchWord.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                GlobalScope.launch(Dispatchers.Main) {
                    delay(2000)
                    userViewModel.getWord(binding.edtSearchWord.text.toString())
                        .observe(viewLifecycleOwner) {
                            when (it.status) {
                                Status.LOADING -> {}
                                Status.ERROR -> {
                                    Log.d(TAG, "onCreateView: error $ edittext retrofit")
                                }
                                Status.SUCCESS -> {
                                    try {
                                        Log.d(TAG, "onCreateView: ${it.data}")
                                        myWord = it.data?.get(0)
                                        binding.wordName.text = myWord?.word
                                        binding.tvDefinition.text =
                                            myWord!!.meanings[0].definitions[0].definition
                                        binding.tvExample.text =
                                            myWord!!.meanings[0].definitions[0].example
                                        writeDb()
                                    } catch (e: Exception) {
                                        binding.wordName.text = null
                                        binding.tvDefinition.text = null
                                        binding.tvExample.text = null

                                        Toast.makeText(
                                            requireContext(),
                                            "Wrong!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }

                                }
                            }
                        }
                }
                handled = true
            }
            handled
        })



        binding.share.setOnClickListener {
            if (myWord != null) {
                try {
                    val share = Intent()
                    share.action = Intent.ACTION_SEND
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    share.putExtra(
                        Intent.EXTRA_TEXT,
                        "${myWord!!.word} \n" +
                                "${myWord!!.meanings[0].definitions[0].definition} \n" +
                                "${myWord!!.meanings[0].definitions[0].example} \n" +
                                "${myWord!!.meanings[0].definitions[0].synonyms} \n" +
                                "${myWord!!.phonetics[0].audio} \n"
                    )
                    startActivity(Intent.createChooser(share, "Share word ..."))
                } catch (e: Exception) {
                    Log.e(TAG, "onCreateView: ${e.message}")
                }
            } else if (myWordDb != null) {
                try {
                    val share = Intent()
                    share.action = Intent.ACTION_SEND
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_SUBJECT, "My application name");
                    share.putExtra(
                        Intent.EXTRA_TEXT,
                        "${myWordDb!!.word} \n" +
                                "${myWordDb!!.definition} \n" +
                                "${myWordDb!!.example} \n" +
                                "${myWordDb!!.audioLink} \n"
                    )
                    startActivity(Intent.createChooser(share, "Share word ..."))
                } catch (e: Exception) {
                    Log.e(TAG, "onCreateView: ${e.message}")
                }
            }
        }

        binding.audio.setOnClickListener {
            if (myWord != null) {
                try {
                    Log.d(TAG, "onCreateView: audi link = ${myWord!!.phonetics[0].audio}")
                    if (myWord!!.phonetics.get(0).audio.isNotEmpty()) {
                        soundBtn(myWord!!.phonetics.get(0).audio)
                    } else if (myWord!!.phonetics.get(1).audio.isNotEmpty()) {
                        soundBtn(myWord!!.phonetics.get(1).audio)
                    } else {
                        Toast.makeText(requireContext(), "no sound", Toast.LENGTH_SHORT)
                            .show()
                    }


                    Toast.makeText(context, "Audio started", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Audio error ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else if (myWordDb != null) {
                try {

                    Log.d(TAG, "onCreateView: audi link = ${myWordDb!!.audioLink}")
                    soundBtn(myWordDb!!.audioLink!!)



                    Toast.makeText(context, "Audio started", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Audio error ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.copy.setOnClickListener { copyEdt() }
        binding.copy2.setOnClickListener { copyEdt() }

        binding.save.setOnClickListener {
            var listD = ArrayList<MyWordDb>()
            listD.addAll(appDatabase.wordDao().getAllMyWordDb())
            for (wordDb in listD) {
                if (wordDb.word == binding.wordName.text.toString()) {
                    if (wordDb.save) {
                        wordDb.save = false
                        appDatabase.wordDao().editWord(wordDb)
                        binding.imgSave.setImageResource(R.drawable.ic_save_1)
                        Toast.makeText(
                            context,
                            "${wordDb.word} saqlanganlardan olib tashlandi",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        wordDb.save = true
                        appDatabase.wordDao().editWord(wordDb)
                        binding.imgSave.setImageResource(R.drawable.ic_save_2)
                        Toast.makeText(context, "${wordDb.word} saqlandi", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        appDatabase = AppDatabase.getInstance(context)
        var list = ArrayList<MyWordDb>()
        list.addAll(appDatabase.wordDao().getAllMyWordDb())
        if (list.isNotEmpty()) {
            myWordDb = list.last()
        }
        if (myWordDb != null) {
            binding.wordName.text = myWordDb?.word
            binding.tvDefinition.text =
                myWordDb!!.definition
            binding.tvExample.text = myWordDb!!.example
            if (myWordDb!!.save) {
                binding.imgSave.setImageResource(R.drawable.ic_save_2)
            } else {
                binding.imgSave.setImageResource(R.drawable.ic_save_1)
            }
        }
    }

    fun soundBtn(url: String) {
        Log.d("LOG888", "${url}")
        Log.d("LOG888", "URL : ${url.substring(url.length - 3, url.length)}")

        if (url.length > 5 && url.substring(url.length - 3, url.length).equals("mp3")) {
            mediaPlayer = MediaPlayer()
            mediaPlayer?.setDataSource(url)
            mediaPlayer?.setOnPreparedListener(this)
            mediaPlayer?.prepareAsync()
        } else {
            Toast.makeText(requireContext(), "Sound format exeption", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeDb() {
        val listDb = appDatabase.wordDao().getAllMyWordDb()
        var has = true
        if (listDb.isNotEmpty()) {
            listDb.forEach {
                if (it.word == myWord?.word) {
                    has = false
                }
            }
            if (has) {
                appDatabase.wordDao().addWord(
                    MyWordDb(
                        myWord!!.word,
                        myWord!!.meanings[0].definitions[0].definition,
                        myWord!!.meanings[0].definitions[0].example,
                        myWord!!.phonetics[0].audio,
                        false
                    )
                )
            }
        } else {
            appDatabase.wordDao().addWord(
                MyWordDb(
                    myWord!!.word,
                    myWord!!.meanings[0].definitions[0].definition,
                    myWord!!.meanings[0].definitions[0].example,
                    myWord!!.phonetics[0].audio,
                    false
                )
            )
        }
    }

    fun copyEdt() {
        val cli = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cli.setPrimaryClip(ClipData.newPlainText("copy to", binding.edtSearchWord.text.toString()))
        Toast.makeText(context, "Copy ${binding.edtSearchWord.text.toString()}", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onPrepared(mp: MediaPlayer?) {
        mediaPlayer?.start()
    }
}