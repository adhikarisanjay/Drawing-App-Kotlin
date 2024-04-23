package com.example.drawingapplicationkotlin

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import yuku.ambilwarna.AmbilWarnaDialog
import yuku.ambilwarna.AmbilWarnaDialog.OnAmbilWarnaListener
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.Random

class MainActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var drawingView: DrawingView
    private  lateinit var brushButton: ImageButton
    private lateinit var greenButton: ImageButton
    private  lateinit var redButton: ImageButton
    private lateinit var blueButton: ImageButton
    private  lateinit var orangeButton: ImageButton
    private lateinit var purpleButton: ImageButton
    private lateinit var undoButton: ImageButton
    private  lateinit var colorpickerButton:ImageButton
    private  lateinit var  galleryButton: ImageButton
    private  lateinit var saveButton:ImageButton

    // gallery launcher
    private  val openGalleryLauncher:ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            findViewById<ImageView>(R.id.gallery_image).setImageURI(result.data?.data)
        }
    val requestPermission: ActivityResultLauncher<Array<String>> = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ premissions ->
        premissions.entries.forEach{
            val permissionName=  it.key
            val isGranted = it.value
            if(isGranted && permissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE){
                Toast.makeText(this,"Permission Granted", Toast.LENGTH_SHORT).show()
                //opening gallery
                val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                openGalleryLauncher.launch(pickIntent)

            }else{
                if (permissionName == android.Manifest.permission.READ_EXTERNAL_STORAGE){
                    Toast.makeText(this,"Permission denied",Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        brushButton = findViewById(R.id.brush_button)

        redButton = findViewById(R.id.red_button)
        orangeButton= findViewById(R.id.orange_button)
        blueButton = findViewById(R.id.blue_button)
        greenButton = findViewById(R.id.green_button)
        purpleButton = findViewById(R.id.purple_button)
        undoButton = findViewById(R.id.undo_button)
        colorpickerButton = findViewById(R.id.color_picker_button)
         galleryButton =findViewById(R.id.button_gallery)
        drawingView = findViewById(R.id.drawingView)
        drawingView.changeBrushSize(23.toFloat())
        saveButton = findViewById(R.id.button_save)



        brushButton.setOnClickListener{
            showBrusChooserDialog()
        }

        redButton.setOnClickListener(this)
        orangeButton.setOnClickListener(this)
        blueButton.setOnClickListener(this)
        greenButton.setOnClickListener(this)
        purpleButton.setOnClickListener(this)
        undoButton.setOnClickListener(this)
        colorpickerButton.setOnClickListener(this)
        galleryButton.setOnClickListener(this)
        saveButton.setOnClickListener(this)
    }

    private  fun showBrusChooserDialog(){
        var brushDialog= Dialog(this@MainActivity)
        brushDialog.setContentView(R.layout.dialog_brush)
        val seekBarProgress = brushDialog.findViewById<SeekBar>(R.id.dialog_seek_bar)
        val showProgressTv = brushDialog.findViewById<TextView>(R.id.dialog_textview)

        seekBarProgress.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, p1: Int, p2: Boolean) {
                drawingView.changeBrushSize(seekBar.progress.toFloat())
                showProgressTv.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }


        })
        brushDialog.show()

    }

    override fun onClick(view: View?) {
        Log.d("color button","${view?.id}")
        when(view?.id){
            R.id.purple_button ->{
                drawingView.setColor("#B200B7")
            }
            R.id.red_button ->{
                drawingView.setColor("#D40808")
            }
            R.id.green_button ->{
                drawingView.setColor("#2DC40B")
            }
            R.id.blue_button ->{
                drawingView.setColor("#2F6FF1")
            }
            R.id.orange_button ->{
                drawingView.setColor("#BD7A06")
            }
            R.id.undo_button->{
                drawingView.undoPath()
            }
            R.id.color_picker_button->{
                showColorPickerDialog()
            }
            R.id.button_gallery->{
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    requestStoragePermission()
                }
                else{
                    //opening gallery to get image
                    val pickIntent=Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(pickIntent)
                }


            }
            R.id.button_save->{
                // save image


            }
        }
    }
   private fun showColorPickerDialog(){
    val dialog = AmbilWarnaDialog(this,Color.GREEN,object:OnAmbilWarnaListener{
        override fun onCancel(dialog: AmbilWarnaDialog?) {

        }

        override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
            drawingView.setColor(color)
        }
    })
        dialog.show()

    }
    private  fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
            showRationalDialog()
        }else{
            requestPermission.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    private  fun  showRationalDialog(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Storage Permission").setMessage("we need this permission in order to  access the internal storage"
        ).setPositiveButton(R.string.dialog_yes){
            dialog, _ ->
            requestPermission.launch(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE))

            dialog.dismiss()
        }
        builder.create().show()
    }

    private  fun getBitMapFromView(view: View):Bitmap{
        val  bitmap = Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(bitmap)
        view.draw(canvas)
        return  bitmap
    }

    private  fun saveImage(bitmap:Bitmap){
        val root =  Environment.getExternalStorageDirectory().toString()
        val myDir = File("root/saved_images")
        myDir.mkdir()
        val generator = Random()
        var n = 10000
        val outputFile =  File(myDir,"Images-$n.jpg")
        if (outputFile.exists()){
            outputFile.delete()
        }else{
            try {
                    val out = FileOutputStream(outputFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG,90,out)
                out.flush()
                out.close()
            }catch (e:Exception){

            }
        }
    }
}