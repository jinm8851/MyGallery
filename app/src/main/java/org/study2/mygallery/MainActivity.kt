package org.study2.mygallery

import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.checkSelfPermission
import org.study2.mygallery.databinding.ActivityMainBinding
import java.util.jar.Manifest


/*저장된 미디어데이터는 콘텐츠 프로바이더를 샤용해 다른 애ㅃ에 공개될수 있습니다
* 콘텐츠 프로바이더란 앱의 데이터 접근을 다른 앱에 허용하는 컴포넌트있니다.
* 1. 외부저장소 읽기 권한부여
* 2 위험 권한 허용
* 3 contentResolver 객체를 이용하여 데이터를 Cursor 객체로 가지고 옵니다.
*
* (안드로이드 4 대 컴포넌트)
* 액티비티 (화면구성)
* 콘텐츠 프로바이더 (데이터베이스, 파일, 네트워크의 데이터를 다른 앱에 공유합니다.
* 브로드캐스트 리시버 (앱이나 기기가 발송하는 방송을 수신합니다.
* 서비스 (화면이 없고 백그라운드 작업에 용이합니다.
*
* 안드로이드 저장소
* 내부저장소 : OS가 설치된영역으로 유저가 접근할 수 없는 시스템영역 앱이 사용하는 정보와 데이터를저장
* 외부저장소 : 유저가 사용하는 영역 사진과 동영상은 외부저장소에 저장됨
* 사용자가 권한을 요청하면 권한이 부여되었는지 확인하고 처리하는 코드는 registerForActivityResult() 함수를 활용합니다.
* 인자로 RequestPermission() 객체를 전달하면 권한에 대한 처리를 할수 있습니다.*/



class MainActivity : AppCompatActivity() {

    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//        권한요청에 대한 처리를 작성하는 부분
            if (isGranted) {
                // 권한 혀용됨
                getAllPhotos()
            } else {
                // 권한 거부
                Toast.makeText(this,"권한 거부됨", Toast.LENGTH_LONG).show()
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//     1.   권한이 부여 됬는지 확인
        if (ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
         != PackageManager.PERMISSION_GRANTED) {
            /* 2. 이전에 권한이 허용되지 않음 shouldShowRequestPermissionRationale() 메서드는 사용자가
                전에 권한 요청을 거부했는지를 반환합니다 true를 반환하면 거부를 한적이 있는 겁니다 */
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            ) {
                // 3. 이전에 이미 권한이 거부되었을 때 설명
                AlertDialog.Builder(this).apply {
                    setTitle("권한이 필요한 이유")
                    setMessage("사진 정보를 얻으려면 외부 저장소 권한이 필요합니다.")
                    setPositiveButton("권한요청") {
                        _, _ ->
                        // 권한 요청
                        requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    setNegativeButton("거부",null)
                }.show()
            } else {
                /* 4. 권한 요청 requestPermissionLauncher 객체를 통해 외부 저장소 읽기 권한을 요청합니다.
                requestPermissionLauncher 객체는 권한이 요청되면 어떤 처리를  해야할지 로직을 작성해 두는 객체입니다
                registerForActivityResult() 함수에 ActivityResultContracts.RequestPermission() 객체를 지정하면 됩니다.*/

                requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            return
        }
        getAllPhotos()
    }


//    모든 사진을 가져오는 코드 작성
    private fun getAllPhotos() {
        val uris = mutableListOf<Uri>()

//    모든 사진 정보 가져오기
    contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //어떤데이터를 가져오느냐를 URI 형태로 지정
    null, // 어떤 항목의 데이터를 가져올 것인지 String 배열로 지정 null을 지정하면 모든 항목을 가져옴
    null,  // 가져올 조건을 지정 전체 데이터를 가져올때는 null을 지정
    null,  // 세번째 인자와 조합하여 조건을 지정할때 사용,사용하지안을땐 null
    "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"  // 찍은 날짜 내림차순
    )?.use {cursor ->   // 콘텐츠 프로바이더로 얻은 모든 사진정보 cursor 객체는 사용을 마치면 반드시 close() 메서드로 닫아야 합니다.
        //Cursor 는 Closeable 인터페이스를 상속한 인터페이스임 use() 확장함수를 사용하면 사용이 끝나고 자동으로 close() 메서드를 호출해줌
        while(cursor.moveToNext()) {
            // 사진정보 id
                /* 사진은 고유한 URI 를 가지고 있고 이것으로 사진을 불러올수 있습니다.
                * 사진의 URI 는 절대적인 경로가 아닌 안드로이드 내부에서 식별하기 위한 별도의 규칙에 의해 정의된 값입니다.
                * 이 URI 를 얻기 위해서 미디어를 가리키는  URI 인 MediaStore.Images.Media.EXTERNAL_CONTENT_URI 에
                * 사진이 저장되 DB 의 id 를 붙이는 형태의 URI를 얻어야 합니다
                * ContentUris 클래스를 사용하면 이러한 URI 결합을 쉽게 할 수 있습니다.*/
            val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            // Uri 얻기
            val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,id)
            // 사진의 Uri들을 리스트에 담기
            uris.add(contentUri)
        }
    }
    Log.d("MainActivity","getAllPhotos: $uris")
    }
}