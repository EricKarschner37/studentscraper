package com.eric.karschner.studentscraper

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.InputType
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.*
import org.jetbrains.anko.recyclerview.v7.recyclerView
import org.json.JSONObject
import java.net.URL
import java.io.InputStreamReader


class MainActivity : AppCompatActivity() {

    var existingServices : String = ""

    val READ_BLOCK_SIZE = 100

    val services : ArrayList<Service> = ArrayList()

    // A list of services to populate the "Add Service" Fragment
    val serviceAddList : ArrayList<ServiceSelection> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(my_toolbar)

        readServicesFromStorage()

        serviceAddList.add(ServiceSelection("Cengage", R.drawable.ic_cengage))
        serviceAddList.add(ServiceSelection("Yabla", R.drawable.ic_yabla))


        services_rv.layoutManager = LinearLayoutManager(this)
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
            readServicesFromStorage()
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

    // Define logic to contact the server and get information about a service
    fun getService(name: String, user : String) : Service{
        val service = Service()
        service.name = name
        service.user = user
        when(name){
            "Cengage" -> {
                service.imageid = R.drawable.ic_cengage
                service.url = "https://nglsync.cengage.com/portal/Account/LogOn?ReturnUrl=%2fportal%2f"
            }
            "Yabla" -> {
                service.imageid = R.drawable.ic_yabla
                service.url = "https://spanish.yabla.com/videos.php"
            }

            else -> {
                service.imageid = R.drawable.ic_error
            }
        }

        doAsync {
            val url = "http://10.36.2.66:3000/service/$name/$user"
            Log.i("Services url", url)
            val result = URL(url).readText()
            Log.i("MainActivity Result", result)
            val JObject = JSONObject(result)
            service.url = JObject["URL"].toString()
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
                        inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD
                    }
                    positiveButton("Add") {
                        Log.i("AddService", "Button Pressed")
                        addServiceRequest(name, user.text.toString(), pass.text.toString())
                    }
                }
            }
        }.show()
    }

    fun addServiceRequest(name: String, user: String, pass: String){

        // Prevent blank fields for the username and/or password
        if (user == "" || pass == ""){
            alert("Either your username or your password was invalid. Please try again."){
                title="Input Error"
                positiveButton("Ok"){}
            }.show()
            return
        } else {
            //TODO - sanitize input
        }

        doAsync {
            // Contact the server to add the service
            //val result = URL("http://10.36.2.66:3000/add/$name/$user/$pass").readText()
            //Log.i("AddServiceResult", result)
            Log.i("AddService", "In doAsync")
            uiThread {
                val waiting = indeterminateProgressDialog("Adding Service")
                Log.i("AddService", "In uiThread")
                onComplete {
                    waiting.dismiss()
                    writeServiceToStorage(name, user)
                }
            }
        }
    }

    fun writeServiceToStorage(name: String, user: String){
        val fOut = openFileOutput("Services.txt", Context.MODE_PRIVATE)

        // Prevent users from adding the same account for the same website twice
        if ("$name,$user" in existingServices.split("|")){
            fOut.write((existingServices).toByteArray())
            alert("That username is already registered for that service"){
                title="Duplicate Service"
                positiveButton("Ok"){}
            }.show()

        // Prevent blank fields for the username
        } else {
            fOut.write(("$existingServices|$name,$user").toByteArray())
        }
        fOut.close()
        readServicesFromStorage()
    }

    fun readServicesFromStorage(){
        val fIn = openFileInput("Services.txt")
        val isr = InputStreamReader(fIn)
        val inputBuffer = CharArray(READ_BLOCK_SIZE)
        existingServices = ""
        var charRead: Int

        charRead = isr.read(inputBuffer)
        while ((charRead) > 0) {
            // char to string conversion
            val readstring = String(inputBuffer, 0, charRead)
            existingServices += readstring
            charRead = isr.read(inputBuffer)
        }

        fIn.close()
        isr.close()

        // We're going to reload ALL of the services, including the previously added ones, so we need to clear here
        services.clear()
        for (service in existingServices.split("|")) {
            // Sometimes due to the storage format we get empty strings here. We don't want to add those.
            if (service != "") {
                val serviceSplit = service.split(",")
                services.add(getService(serviceSplit[0], serviceSplit[1]))
            }
        }
        val adapter = ServiceAdapter(services, this)

        // Let the delete icon actually work by defining the ability to remove services
        adapter.onItemClick = { service ->

            doAsync {
                //TODO - Remove from server
                onComplete {
                    // Sometimes the formatting of the storage file gets messed up.
                    // This redundancy here helps prevent errors
                    existingServices = existingServices.replace("${service.name},${service.user}|","")
                    existingServices = existingServices.replace("|${service.name},${service.user}","")
                    existingServices = existingServices.replace("${service.name},${service.user}","")
                    val fOut = openFileOutput("Services.txt", Context.MODE_PRIVATE)
                    fOut.write(existingServices.toByteArray())
                    fOut.close()

                    // Update the list now that the service has been removed
                    readServicesFromStorage()
                }
            }
        }
        services_rv.adapter = adapter
    }
}

//Stores the data for an already added service
data class Service(var name: String = "Default Name", var url: String = "https://www.google.com", var imageid: Int = 0,
                   var user: String = "Default", var assignments: String = "0", var date: String = "1/1")

//Stores image/name data for the service selections to be added
data class ServiceSelection(var name: String = "Default Name", var imageid: Int)
