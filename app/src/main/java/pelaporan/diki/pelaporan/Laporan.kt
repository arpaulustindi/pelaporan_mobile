package pelaporan.diki.pelaporan

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.diki.pelaporan.R
import okhttp3.MultipartBody
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


import android.os.Environment
import android.provider.OpenableColumns
import android.view.View

import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_laporan.*

import okhttp3.MediaType

import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class Laporan : AppCompatActivity() {
    lateinit var _nama : EditText
    lateinit var _hp : EditText
    lateinit var _detail : EditText
    lateinit var _metadata: EditText

    private lateinit var buttonKirim: Button
    private  lateinit var lokasiFile: String
    private var selectedImageUri: Uri? = null
    private lateinit var image_view: ImageView
    private lateinit var btnPilih: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_laporan)

        val imageView: ImageView = findViewById(R.id.imageView)
        image_view = findViewById(R.id.imageView)
        val nama : EditText = findViewById(R.id.txtName)
        val bundle: Bundle? = intent.extras!!
        //val resId: Bitmap = bundle?.getInt("resId") as Bitmap
        //val resId: String = bundle?.getString("resId") as String
        val metaData: String = bundle?.getString("metadata") as String
        //lokasiFile = resId
        //val imgFile: File =  File(resId)
        //if(imgFile.exists()){
            //val myBitmap: Bitmap = BitmapFactory.decodeFile(imgFile.toString())
            //imageView.setImageBitmap(myBitmap)
        txtMeta.setText(metaData)


        //}
        //val imageBitmap = bundle?.get("resId") as Bitmap
        //imageView.setImageBitmap(imageBitmap)
        buttonKirim = findViewById(R.id.btnKirim)
        buttonKirim.setOnClickListener{
            //PRINT--
            println("0  Klik Upload")
            uploadImage()
        }
        btnPilih  = findViewById(com.diki.pelaporan.R.id.btnPilih)
        btnPilih.setOnClickListener {
            opeinImageChooser()
        }



    }
    private fun opeinImageChooser() {

        Intent(Intent.ACTION_PICK).also {
            it.type = "image/*"
            val mimeTypes = arrayOf("image/jpeg", "image/png")
            it.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            startActivityForResult(it, REQUEST_CODE_IMAGE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> {
                    selectedImageUri = data?.data
                    image_view.setImageURI(selectedImageUri)
                }
            }
        }
    }

    companion object {
        const val REQUEST_CODE_IMAGE = 101
    }

    private fun ContentResolver.getFileName(selectedImageUri: Uri): String {
        var name = ""
        val returnCursor = this.query(selectedImageUri,null,null,null,null)
        if (returnCursor!=null){
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

    private fun View.snackbar(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("OK") {
                snackbar.dismiss()
            }
        }.show()

    }

    private fun View.snackbar2(message: String) {
        Snackbar.make(this, message, Snackbar.LENGTH_LONG).also { snackbar ->
            snackbar.setAction("OK") {
                snackbar.dismiss()
                val intent = Intent(application, ArcoreMeasurement::class.java)
                startActivity(intent)
            }
        }.show()

    }
    private fun uploadImage() {
        if (selectedImageUri == null){
            layout_root.snackbar("Silahkan Pilih Gambar")
            return
        }
        val parcelFileDescriptor = contentResolver.openFileDescriptor(
            selectedImageUri!!, "r", null
        ) ?: return

        _nama = findViewById(R.id.txtName)
        _hp = findViewById(R.id.txtHp)
        _detail = findViewById(R.id.txtDetail)
        _metadata = findViewById(R.id.txtMeta)

        //val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        //val file = File(lokasiFile)

        val inputStream = FileInputStream(parcelFileDescriptor.fileDescriptor)
        val file = File(cacheDir, contentResolver.getFileName(selectedImageUri!!))
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)


        val body = UploadRequestBody(file,"image", this)
        MyApi().uploadImage(
            MultipartBody.Part.createFormData(
            "gambar",
            file.name,

            body
        ),
            RequestBody.create(MediaType.parse("multipart/form-data"),_nama.text.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"),_hp.text.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"),_detail.text.toString()),
            RequestBody.create(MediaType.parse("multipart/form-data"),_metadata.text.toString())
        ).enqueue(object : Callback<UploadResponse>{
            override fun onResponse(
                call: Call<UploadResponse>,
                response: Response<UploadResponse>
            ) {
                response.body()?.let {
                    layout_root.snackbar2("Berhasil Upload")
                    //progress_bar.progress = 100
                    println("Berhasil : $it.message")

                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                layout_root.snackbar(t.message!!)
                //progress_bar.progress = 0
                println("Kesalahan API : $t.message!!")
            }

        })
        //PRINT--
        println("2 Enque : Di enque")

    }


}