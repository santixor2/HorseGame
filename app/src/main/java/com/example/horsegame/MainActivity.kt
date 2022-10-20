package com.example.horsegame

import android.graphics.Point
import android.icu.text.CaseMap
import androidx.appcompat.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit
import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.core.content.contentValuesOf
import androidx.test.runner.screenshot.ScreenCapture
import androidx.test.runner.screenshot.Screenshot.capture
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null

    private var callSellected_x = 0
    private var callSellected_y = 0
    private var mHandler: Handler? = null
    private var timeInSeconds: Long = 0
    private var gaming = true
    private var string_share = ""
    private var level = 1

    private var widht_bonus = 0
    private var levelMoves = 64
    private var checkMovement = true

    private var movesRequired = 4
    private var moves = 64
    private var options = 0
    private var bonus = 0
    private var nameColorBlack = "blackCell"
    private var nameColorWhite = "whiteCell"

    private lateinit var board: Array<IntArray>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initScreenGame()
        starGame()
    }
    fun checkCellClicked(v: View){

        var name = v.tag.toString()
        var x = name.subSequence(1,2).toString().toInt()
        var y = name.subSequence(2,3).toString().toInt()
        checkCell(x, y)
    }
    private fun checkCell(x: Int, y: Int){

        var checkTrue = true

        if(checkMovement){
            var dif_x = x - callSellected_x
            var dif_y = y - callSellected_y

            checkTrue = false
            if( dif_x == 1 && dif_y == 2) checkTrue = true
            if( dif_x == 1 && dif_y == -2) checkTrue = true
            if( dif_x == 2 && dif_y == 1) checkTrue = true
            if( dif_x == 2 && dif_y == -1) checkTrue = true
            if( dif_x == -1 && dif_y == 2) checkTrue = true
            if( dif_x == -1 && dif_y == -2) checkTrue = true
            if( dif_x == -2 && dif_y == 1) checkTrue = true
            if( dif_x == -2 && dif_y == -1) checkTrue = true
        }
        else{
            if(board[x][y] != 1){
                bonus--
                var tvBonusData = findViewById<TextView>(R.id.tvBonusData)
                tvBonusData.text = " + $bonus"
                if(bonus == 0) tvBonusData.text = ""
            }
        }

        if(board[x][y] == 1) checkTrue = false
        if(checkTrue) selectCell(x, y)

    }
    private fun resetBoard(){
        board = arrayOf(
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
            intArrayOf(0, 0, 0, 0, 0, 0, 0, 0),
        )
    }
    private fun clearBoard(){
        var iv : ImageView

        var colorBlack = ContextCompat.getColor(this,
        resources.getIdentifier(nameColorBlack, "color", packageName))

        var colorWhite = ContextCompat.getColor(this,
            resources.getIdentifier(nameColorWhite, "color", packageName))

        for (i in 0..7){
            for (j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j", "id", packageName))
               // iv.setImageResource(R.drawable.horse)
                iv.setImageResource(0)
            }
        }
    }
    private fun setFirstPositions(){
        var x = 0
        var y = 0
        x = (0..7).random()
        y = (0..7).random()

        callSellected_x = x
        callSellected_y = y

        selectCell(x, y)
    }
    private fun growProgressBonus(){

        var moves_done = levelMoves - moves
        var bonus_done = moves_done / movesRequired
        var moves_rest = movesRequired * (bonus_done)
        var bonus_grow = moves_done - moves_rest

        var v = findViewById<View>(R.id.vNewBonus)
        var widthBonus = ((widht_bonus/movesRequired) * bonus_grow).toFloat()
        var height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics()).toInt()
        var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthBonus, getResources().getDisplayMetrics()).toInt()
        v.setLayoutParams(TableRow.LayoutParams(width, height))

    }
    private fun selectCell(x: Int , y: Int){

        moves--
        var tvMovesData = findViewById<TextView>(R.id.tvOptionsData)
        tvMovesData.text = moves.toString()

        growProgressBonus()

        if(board[x][y] == 2){
            bonus++
            var tvBonusData = findViewById<TextView>(R.id.tvBonusData)
            tvBonusData.text = " + $bonus"
        }

        board[x][y] = 1
        paintHorseCell(callSellected_x, callSellected_y, "previusCell")
        callSellected_x = x
        callSellected_y = y

        clearOptions()

        paintHorseCell(x, y, "selectedCell")
        checkMovement = true
        checkOption(x, y)

        if(moves > 0 ){
            checkNewBonus()
            checkGameOver(x, y)
        }
        else showMessage("You WIN", "Next Level", false)
    }
    private fun checkGameOver(x: Int, y: Int){
        if(options == 0){

            if (bonus > 0){
                checkMovement = false
                paintAllOptions()
            }
            else showMessage("Game over", "Try again", true)

        }

    }
    private fun paintAllOptions(){
        for (i in 0..7){
            for (j in 0..7) {
                if (board[i][j] != 1) paintOptions(i, j)
                if (board[i][j] == 0) board[i][j] = 9
            }
        }
    }
    private fun  showMessage(title: String, action: String, gameOver: Boolean){
        gaming = false
        var lyMessage = findViewById<LinearLayout>(R.id.lyMessage)
        lyMessage.visibility = View.VISIBLE

        var tvTitleMessage = findViewById<TextView>(R.id.tvTitleMessage)
        tvTitleMessage.text = title

        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)
        var score: String = ""

        if (gameOver){
            score = "Score: " + (levelMoves-moves) + "/" + levelMoves
            string_share = "this game makes me sick!! " + score + " google.com"

        }
        else{
            score = tvTimeData.text.toString()
            string_share = "lets go!!! new challenge completed. Level: $level (" + score + ") google.com"

        }

        var tvScoreMessage = findViewById<TextView>(R.id.tvScoreMessage)
        tvScoreMessage.text = score

        var tvAction = findViewById<TextView>(R.id.tvAction)
        tvAction.text = action

    }
    private fun checkNewBonus(){
        if(moves % movesRequired == 0){
            var bonusCell_x = 0
            var bonusCell_y = 0
            var bonusCell = false
            while(bonusCell == false){
                bonusCell_x = (0..7).random()
                bonusCell_y = (0..7).random()

                if(board[bonusCell_x][bonusCell_y] == 0) bonusCell = true
            }
            board[bonusCell_x][bonusCell_y] = 2
            paintBonusCell(bonusCell_x, bonusCell_y)
        }

    }
    private fun paintBonusCell(x: Int, y: Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        iv.setImageResource(R.drawable.alasbonus)

    }
    private fun clearOption(x: Int, y: Int){
        var iv : ImageView = findViewById(resources.getIdentifier("c$x$y", "id", packageName))
        if(checkColorCell(x, y) == "black")
            iv.setBackgroundColor(ContextCompat.getColor(this,
              resources.getIdentifier(nameColorBlack, "color", packageName)))
        else
            iv.setBackgroundColor(ContextCompat.getColor(this,
              resources.getIdentifier(nameColorWhite, "color", packageName)))

        if (board[x][y] == 1) iv.setBackgroundColor(ContextCompat.getColor(this,
            resources.getIdentifier("previusCell", "color", packageName)))

    }
    private fun clearOptions(){
        for (i in 0..7){
            for (j in 0..7){
                if(board[i][j] == 9 || board[i][j] == 2){
                    if(board[i][j] == 9)  board[i][j] = 0
                    clearOption(i, j)
                }
            }
        }
    }
    private fun checkOption(x: Int, y: Int){
        options = 0

        checkMove(x, y, 1, 2)
        checkMove(x, y, 2, 1)
        checkMove(x, y, 1, -2)
        checkMove(x, y, 2, -1)
        checkMove(x, y, -1, 2)
        checkMove(x, y, -2, 1)
        checkMove(x, y, -1, -2)
        checkMove(x, y, -2, -1)

        var tvOptionsData = findViewById<TextView>(R.id.tvOptionsData)
        tvOptionsData.text = options.toString()

    }
    private fun checkMove(x: Int, y: Int, mov_x: Int, mov_y: Int){
        var option_x = x + mov_x
        var option_y = y + mov_y

        if(option_x < 8 && option_y < 8 && option_x >= 0 && option_y >= 0){
            if(board[option_x][option_y]== 0
                || board[option_x][option_y] == 2){
                options++
                paintOptions(option_x, option_y)
               if(board[option_x][option_y] == 0) board[option_x][option_y] = 9
            }
        }
    }

    private fun checkColorCell(x: Int, y: Int): String{
        var color = ""
        var blackColumn_x = arrayOf(0,2,4,6)
        var blackRow_x = arrayOf(1,3,5,7)
        if((blackColumn_x.contains(x) && blackColumn_x.contains(y))
            || (blackRow_x.contains(x) && blackRow_x.contains(y)))
            color = "black"
        else color = "white"
        return color

    }

    private fun paintOptions(x: Int, y: Int){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y","id", packageName))
        if (checkColorCell(x, y) == "black") iv.setBackgroundResource(R.drawable.option_black)
        else iv.setBackgroundResource(R.drawable.option_white)

    }
    private fun paintHorseCell(x: Int, y: Int, color: String){
        var iv: ImageView = findViewById(resources.getIdentifier("c$x$y","id", packageName))
        iv.setBackgroundColor(ContextCompat.getColor(this, resources.getIdentifier(color, "color", packageName)))
        iv.setImageResource(R.drawable.horse)

    }

    private fun initScreenGame(){
        setSizeBoard()
        hideMessage()
    }
    private fun setSizeBoard(){
        var iv: ImageView
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        var width_dp = (width / getResources().getDisplayMetrics().density)
        var lateralMarginDP = 0
        val width_cell = (width_dp - lateralMarginDP)/8
        val heigth_cell = width_cell

        widht_bonus = 2 * width_cell.toInt()

        for (i in 0..7){
            for (j in 0..7){
                iv = findViewById(resources.getIdentifier("c$i$j","id", packageName))
                var height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heigth_cell, getResources().getDisplayMetrics()).toInt()
                var width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heigth_cell, getResources().getDisplayMetrics()).toInt()
                iv.setLayoutParams(TableRow.LayoutParams(width, height))
            }
        }


    }
    private fun hideMessage(){
        var lyMessage = findViewById<LinearLayout>(R.id.lyMessage)
        lyMessage.visibility = View.INVISIBLE
    }
    @RequiresApi(Build.VERSION_CODES.N)
    fun launchShareGame(v: View){
        sharaGame()
    }
    @RequiresApi(Build.VERSION_CODES.N)
    private fun sharaGame(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        var ssc: ScreenCapture = capture(this)
        bitmap = ssc.getBitmap()
        if(bitmap != null){
            var idGame = SimpleDateFormat("yyyy/mm/dd").format(Date())
            idGame = idGame.replace(":", "")
            idGame = idGame.replace("/", "")
            val path = saveImage(bitmap, "${idGame}.jpg")
            val bmpUri = Uri.parse(path)

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri)
            shareIntent.putExtra(Intent.EXTRA_TEXT, string_share)
            shareIntent.type = "image/png"

            val finalShareIntent = Intent.createChooser(shareIntent, "select the app you want to share the game to")
            finalShareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            this.startActivity(finalShareIntent)



        }
    }
    private fun saveImage(bitmap: Bitmap?, fileName: String): String?{
        if(bitmap == null)
            return null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q){

            val contentValues = ContentValues().apply{
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/screenshot")

            }
            val uri = this.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if(uri != null){
                this.contentResolver.openOutputStream(uri).use{
                    if(it == null)
                        return@use

                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, it)
                    it.flush()
                    it.close()

                    MediaScannerConnection.scanFile(this, arrayOf(uri.toString()), null, null)

                }
            }
            return uri.toString()

        }
        val filePath = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES + "/Screenshot"
        ).absolutePath

        val dir = File(filePath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, fileName)
        val fOut = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut)
        fOut.flush()
        fOut.close()

        MediaScannerConnection.scanFile(this, arrayOf(file.toString()), null, null)
        return filePath

    }


    private fun resetTime(){
        mHandler?.removeCallbacks(chronometer)
        timeInSeconds = 0
        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)
        tvTimeData.text = "00:00"

    }
    private fun starTime(){
        mHandler = Handler(Looper.getMainLooper())
        chronometer.run()
    }
    private var chronometer: Runnable = object: Runnable{
        override fun run(){
            try{
                if(gaming){
                    timeInSeconds++
                    updateStopWatchView(timeInSeconds)
                }

            }finally {
                mHandler!!.postDelayed(this, 1000L)
            }
        }
    }
    private fun updateStopWatchView(timeInSeconds: Long){
        val formattedTime = getFormattedStopWatch((timeInSeconds * 1000))
        var tvTimeData = findViewById<TextView>(R.id.tvTimeData)
        tvTimeData.text = formattedTime

    }
    private fun getFormattedStopWatch(ms: Long): String{
        var milliSeconds = ms
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliSeconds)
        milliSeconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliSeconds)
        return "${if(minutes > 10) "0" else "" }$minutes:" +
                "${if(seconds > 10) "0" else ""}$seconds"
        }


        private fun starGame(){

            gaming = true
            resetBoard()
            clearBoard()
            setFirstPositions()
            resetTime()
            starTime()
        }
    }
