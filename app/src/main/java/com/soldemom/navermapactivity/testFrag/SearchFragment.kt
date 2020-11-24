package com.soldemom.navermapactivity.testFrag

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.soldemom.navermapactivity.DetailActivity
import com.soldemom.navermapactivity.Point
import com.soldemom.navermapactivity.R
import com.soldemom.navermapactivity.kakaoLocal.RetrofitHelper
import com.soldemom.navermapactivity.kakaoLocal.RetrofitService
import kotlinx.android.synthetic.main.fragment_search.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    lateinit var docAddr: DocAddr
    val db = FirebaseFirestore.getInstance()
    var studyList = mutableListOf<Point>()
    lateinit var adapter : SearchAdapter
    lateinit var retrofitService: RetrofitService
    var selectedAddress: String = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val x = 126.97794339818097
        val y = 37.5663058756884
        val appKey = getString(R.string.kakao_local_app_key)
        var listView = ListView(requireContext())
        adapter = SearchAdapter(::searchFragToDetailActivity)

        adapter.studyList = studyList
        view.search_recycler_view.adapter = adapter



        val retrofit = RetrofitHelper.getRetrofit()
        retrofitService = retrofit.create(RetrofitService::class.java)
        retrofitService.getByGeo(appKey, x, y).enqueue(object: Callback<DocAddr>{
            override fun onResponse(call: Call<DocAddr>, response: Response<DocAddr>) {
                if (response.isSuccessful) {
                    docAddr = response.body()!!
                    val address = docAddr.documents[1].address_name

                    Log.d("주소",address)
                    db.collection("markers").whereEqualTo("address",address)
                        .get().addOnSuccessListener {
                            studyList = it.toObjects(Point::class.java)
                            adapter.studyList = studyList
                            adapter.notifyDataSetChanged()

                        }
                }
            }

            override fun onFailure(call: Call<DocAddr>, t: Throwable) {
            }
        })

        view.search_button.setOnClickListener {
            val inputAddress = view.search_input.text.toString()
            Log.d("주소","내가 입력한건  $inputAddress")
            retrofitService.getByAdd(appKey,inputAddress).enqueue(object : Callback<DocAddr> {
                override fun onResponse(call: Call<DocAddr>, response: Response<DocAddr>) {
                    if (response.isSuccessful) {
                        val inputResult = response.body()!!


                        Log.d("주소", "검색되서 나온 애는 ${inputResult.documents[0].address_name}")
                        //검색된 주소가 2개 이상이라면
                        if (inputResult.documents.size >= 2) {

                            for (doc in inputResult.documents) {
                                doc.changeDepth1()
                            }

                            val listView = ListView(requireContext())

                            // 다이얼로그에 들어간 listview를 꺼야야함.
                            if (listView.parent != null) {
                                (listView.parent as ViewGroup).removeView(
                                    listView
                                )
                            }

                            val addressList = List<String>(inputResult.documents.size) {
                                inputResult.documents[it].address_name
                            }
                            listView.adapter = ArrayAdapter<String>(
                                requireContext(),
                                android.R.layout.simple_list_item_1,
                                addressList
                            )


                            val dialog = AlertDialog.Builder(requireContext())
                                .setTitle("선택해주세요")
                                .setView(listView)
                                .setNegativeButton("닫기", null)
                                .create()

                            dialog.show()

                            listView.setOnItemClickListener { parent, view, position, id ->
                                selectedAddress = addressList[position]
                                Log.d("안쪽주소", "-$selectedAddress-")

                                db.collection("markers")
                                    .whereEqualTo("address", selectedAddress)
                                    .get()
                                    .addOnSuccessListener {
                                        studyList = it.toObjects(Point::class.java)
                                        adapter.studyList = studyList
                                        adapter.notifyDataSetChanged()
                                        dialog.cancel();
                                    }
                            }




                        }
                        else {
                            inputResult.documents[0].changeDepth1()
                            selectedAddress = inputResult.documents[0].address_name
                            Log.d("안쪽주소", selectedAddress)
                            db.collection("markers")
                                .whereEqualTo("address",selectedAddress)
                                .get()
                                .addOnSuccessListener {
                                    studyList = it.toObjects(Point::class.java)
                                    adapter.studyList = studyList
                                    adapter.notifyDataSetChanged()
                                }


                        }
                    }
                }
                override fun onFailure(call: Call<DocAddr>, t: Throwable) {
                }
            })

            view.search_input.setText("")

        }





        return view
    }

    fun searchFragToDetailActivity(studyId: String) {
        val intent = Intent(requireContext(), DetailActivity::class.java)
        intent.putExtra("studyId",studyId)
        startActivity(intent)
    }

}