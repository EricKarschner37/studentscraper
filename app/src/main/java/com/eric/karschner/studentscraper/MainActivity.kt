package com.eric.karschner.studentscraper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    val services : ArrayList<Service> = ArrayList()
    val serviceNames : ArrayList<ArrayList<String>> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(my_toolbar)

        val yabla : ArrayList<String> = ArrayList()
        yabla.add("Yabla")
        yabla.add("Ekars12")
        val cengage : ArrayList<String> = ArrayList()
        cengage.add("Cengage")
        cengage.add("ekarschner")

        serviceNames.add(yabla)
        serviceNames.add(cengage)
        Log.i("servicepairs", serviceNames.toString())

        //Contact the server for each of the user's services
        for (serviceName in serviceNames) services.add(getService(serviceName[0], serviceName[1]))

        services_rv.layoutManager = LinearLayoutManager(this)
        services_rv.adapter = ServiceAdapter(services, this)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_add -> {
            // User chose the "Add Service" item
            true
        }

        R.id.action_refresh -> {
            // User chose the "Refresh" item
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    //Define logic to contact the server and get information about a service
    fun getService(name: String, user : String) : Service{
        val service = Service()
        doAsync {
            val result = URL("http://73.101.115.19:3000/service/" + name + "/" + user).readText()
            Log.i("MainActivity Result", result)
            val JObject = JSONObject(result)
            service.name = JObject["Name"].toString()
            service.url = JObject["URL"].toString()
            service.imageurl = JObject["ImageURL"].toString()
            service.assignments = JObject["Assignments"].toString()
            service.date = JObject["Date"].toString()
        }
        return service
    }
}

class Service(var name: String = "Default Name", var url: String = "https://www.google.com", var imageurl: String = "defaultImageURL", var assignments: String = "Default", var date: String = "1/1"){
}