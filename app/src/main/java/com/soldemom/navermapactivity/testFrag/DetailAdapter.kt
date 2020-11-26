package com.soldemom.navermapactivity.testFrag

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.soldemom.navermapactivity.R
import com.soldemom.navermapactivity.User

class DetailAdapter(
    val activity: Activity
) : RecyclerView.Adapter<DetailViewHolder>() {

    var memberList = listOf<User>()
    var leader: String = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.detail_memeber_item_list,parent,false)
        return DetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val user = memberList[position]
        holder.memberName.text = user.name

        if (leader == user.uid) {
            holder.memberLeader.apply {
                text = leader
                visibility = View.VISIBLE
            }
        }

//        이미지는 나중에..
//        holder.memberImage
        user.image?.let {imageUri ->
            Glide.with(activity)
                .load(imageUri)
                .into(holder.memberImage)
        }

        holder.memberIntroduce.text = user.introduce


    }

    override fun getItemCount(): Int {
        return memberList.size
    }
}