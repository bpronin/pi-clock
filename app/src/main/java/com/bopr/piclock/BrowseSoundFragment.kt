package com.bopr.piclock

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.view.LayoutInflater.from
import android.widget.RadioButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bopr.piclock.Settings.Companion.PREF_TICK_SOUND
import com.bopr.piclock.util.HandlerTimer
import com.bopr.piclock.util.getStringArray

class BrowseSoundFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var selectedItem: String
    private val itemValues by lazy { getStringArray(R.array.tick_sound_values) }
    private val itemNames by lazy { getStringArray(R.array.tick_sound_names) }
    private val settings: Settings by lazy { Settings(requireContext()) }
    private val timer by lazy { HandlerTimer(Handler(Looper.getMainLooper()), 1000L, 1, ::onTimer) }
    private val player by lazy { TickPlayer(requireContext()) }
    private var repeatsCounter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        selectedItem = settings.getString(PREF_TICK_SOUND)
    }

    override fun onDestroy() {
        stopPlaySound()
        super.onDestroy()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_browse_sound, container, false).apply {
            recycler = findViewById<RecyclerView>(R.id.list).apply {
                adapter = ListAdapter()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.browse_sound, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                requireActivity().finish()
                true
            }
            R.id.action_save -> {
                settings.update { putString(PREF_TICK_SOUND, selectedItem) }
                requireActivity().finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun onTimer(tick: Int) {
        if (tick == 1) {
            player.apply {
                play()
                if (repeatsCounter == 0) {
                    fadeVolume(MAX_REPEATS * 1000L, 1f, 0f)
                }
                if (++repeatsCounter == MAX_REPEATS) {
                    stopPlaySound()
                }
            }
        }
    }

    private fun startPlaySound() {
        stopPlaySound()
        repeatsCounter = 0
        player.setSource(selectedItem)
        timer.enabled = true
    }

    private fun stopPlaySound() {
        timer.enabled = false
        player.stop()
    }

    private inner class Holder(view: View) : ViewHolder(view) {
        val radioButton: RadioButton by lazy { view.findViewById(R.id.radio_button) }
    }

    private inner class ListAdapter : Adapter<Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(from(requireContext()).inflate(R.layout.list_item_sound, parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.radioButton.apply {
                text = itemNames[position]
                isChecked = itemValues[position] == selectedItem
                setOnClickListener {
                    selectedItem = itemValues[position]
                    notifyDataSetChanged()
                    startPlaySound()
                }
            }
        }

        override fun getItemCount(): Int {
            return itemValues.size
        }
    }

    companion object {

        private const val MAX_REPEATS = 8
    }
}

