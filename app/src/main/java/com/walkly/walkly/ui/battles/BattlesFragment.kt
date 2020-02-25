package com.walkly.walkly.ui.battles

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import com.walkly.walkly.R
import com.walkly.walkly.models.Battle
import kotlinx.android.synthetic.main.fragment_battles.*
import kotlinx.android.synthetic.main.fragment_host_join_battle.*

class BattlesFragment : Fragment() {

    private lateinit var battlesRecyclerView: RecyclerView

    private var adapter: BattleAdapter? = null

    private val battlesViewModel: BattlesViewModel by lazy {
        ViewModelProviders.of(this).get(BattlesViewModel::class.java)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_host_join_battle, container, false)
        battlesRecyclerView = view.findViewById(R.id.battles_recycler_view)


        battlesViewModel.battleList.observe(this, Observer { list ->
            list?.let {
                if(list.isEmpty()) {
                    progressBar.visibility = View.GONE
                } else {
                    adapter = BattleAdapter(list)
                    battlesRecyclerView.adapter = adapter
                    progressBar.visibility = View.GONE
                }
            }
        })



        return view
    }

    private inner class BattleHolder(view: View): RecyclerView.ViewHolder(view), View.OnClickListener {
        val battleName: TextView = itemView.findViewById(R.id.tv_battle_name)
        val battleHost: TextView = itemView.findViewById(R.id.tv_battle_host)
        val playerCount: TextView = itemView.findViewById(R.id.tv_players)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
           // Add your on click logic here
        }
    }

    private inner class BattleAdapter(var battles: List<Battle>): RecyclerView.Adapter<BattleHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleHolder {
            val view = layoutInflater.inflate(R.layout.list_join_battles, parent, false)
            return BattleHolder(view)
        }

        override fun getItemCount(): Int {
            return battles.size
        }

        override fun onBindViewHolder(holder: BattleHolder, position: Int) {
            val battle = battles[position]
            holder.apply {
                // Default image
                battleName.text = battle.battleName
                battleHost.text = battle.host
                playerCount.text = "${battle.playerCount}/4 Players"

            }
         }
    }
}
