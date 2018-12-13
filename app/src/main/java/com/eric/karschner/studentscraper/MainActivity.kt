package com.eric.karschner.studentscraper

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.custom.customView
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    val services : ArrayList<Service> = ArrayList()

    val serviceNames : ArrayList<ArrayList<String>> = ArrayList()

    //A list of services to populate the "Add Service" Fragment
    val serviceAddList : ArrayList<ServiceSelection> = ArrayList()

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

        serviceAddList.add(ServiceSelection("Cengage", "https://pbs.twimg.com/profile_images/2162271613/CengageLogo-01_400x400.png"))
        serviceAddList.add(ServiceSelection("Yabla", "https://pbs.twimg.com/profile_images/757637927480528898/Lbe_Y9vN.jpg"))

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
            lateinit var addAlert: DialogInterface
            addAlert= alert {
                title = "Add Service"
                customView{
                    linearLayout {
                        orientation = LinearLayout.VERTICAL
                        padding = dip(24)
                        recyclerView {
                            layoutManager = LinearLayoutManager(context)
                            var addServiceAdapter = AddServiceAdapter(serviceAddList, context)
                            addServiceAdapter.onItemClick = { selection ->
                                addAlert.dismiss()
                                Log.i("Service name", selection.name)
                                addService(selection.name)
                            }
                            adapter = addServiceAdapter
                        }
                    }
                }
            }.show()
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
            val result = URL("http://10.36.2.66:3000/service/$name/$user").readText()
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

    fun addService(name: String) {
        alert {
            var user: EditText
            var pass: EditText
            title = "Add $name Account"
            customView {
                linearLayout {
                    padding = dip(24)
                    orientation = LinearLayout.VERTICAL
                    textView {
                        text = "Username/Email:"
                    }
                    user = editText {
                        hint = "Username"
                    }
                    textView {
                        text = "Password"
                    }
                    pass = editText {
                        hint = "Password"
                        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
                    }
                    positiveButton("Add") {
                        Log.i("AddService", "Test")
                        addServiceRequest(name, user.text.toString(), pass.text.toString())
                    }
                }
            }
        }.show()
    }

    fun addServiceRequest(name: String, user: String, pass: String){
        doAsync {
            //val result = URL("http://10.36.2.66:3000/add/$name/$user/$pass").readText()
            //Log.i("AddServiceResult", result)
            Log.i("AddService", "In doAsync")
            uiThread {
                Log.i("AddService", "In uiThread")
                val waiting = indeterminateProgressDialog("Adding Service")
                onComplete { waiting.dismiss() }
            }
        }
    }
}

//Stores the data for an already added service
data class Service(var name: String = "Default Name", var url: String = "https://www.google.com", var imageurl: String = "defaultImageURL", var assignments: String = "Default", var date: String = "1/1")

//Stores image/name data for the service selections to be added
data class ServiceSelection(var name: String = "Default Name", var imageurl: String = "Default ImageURL")
