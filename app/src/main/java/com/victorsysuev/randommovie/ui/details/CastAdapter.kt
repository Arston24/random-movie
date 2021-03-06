package com.victorsysuev.randommovie.ui.details

import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.victorsysuev.randommovie.DetailsFragmentDirections
import com.victorsysuev.randommovie.models.Cast
import com.victorsysuev.randommovie.ui.random.RandomMoviesFragmentDirections

class CastAdapter : RecyclerView.Adapter<CastViewHolder>() {

    var castList: ArrayList<Cast> = arrayListOf()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        return CastViewHolder.create(parent)
    }

    override fun getItemCount(): Int {
        return castList.size
    }

    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        val cast = castList[position]
        holder.viewModel.bind(cast)
        holder.itemView.setOnClickListener {
            val action = DetailsFragmentDirections.actionDetailsFragmentToPersonFragment(cast.id.toString())
            Navigation.findNavController(it).navigate(action)
        }
    }
}