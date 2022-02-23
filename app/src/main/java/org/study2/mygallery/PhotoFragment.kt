package org.study2.mygallery

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import coil.load


// 클래스 선언 밖에 const 키워드를 사용하여 상수를 정의하면 컴파일 시간에 결정되는 상수가 되고 이 파일
// 내에서 어디서든 사용할수 있습니다 컴파일 시간 상수의 초기화는 String 또는 Int,Long,Double 등 기본형으로만 됨
private const val ARG_URI = "uri"


class PhotoFragment : Fragment() {


    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getParcelable<Uri>(ARG_URI)?.let {
            uri = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        프레그먼트 뷰 바인딩을 사용하면 메모리 해제도 고려해야 하기때문에 오히려 코드가 더 복잡해집니다(한개라 뷰바인딩사용)
        val imageView = view.findViewById<ImageView>(R.id.imageView)

        /* 프레그먼트에서 콘텐츠 프로바이더에 접근하려면 컨텍스트가 필요합니다. requireContext() 메서드로 얻을수 있습니다.
        * contentResolver 객체로부터 openFileDescriptor() 메서드를 사용하면ParcelFileDescriptor객체(descriptor)를
        * 얻을수 있습니다. 이객체를 통해서 BitmapFactory.decodeFileDescriptor() 메서드를 통해 Bitmap 객체를 얻습니다.
        * Bitmap 객체는 Coil 라이브러리를 사용하여 이미지를 부드럽게 로딩합니다 이미지를 빠르고 부드럽게 로딩하고 메모리 관리까지
        * 자동으로 하고 싶다면 Coil 을 사용하세요 코드는 같은 한줄이지만 성능이 매우 향상됩니다.*/

        val descriptor = requireContext().contentResolver.openFileDescriptor(uri,"r")
//        use{ } 함수는 자동으로 close()해줌
        descriptor?.use {
            val bitmap = BitmapFactory.decodeFileDescriptor(descriptor.fileDescriptor)
            imageView.load(bitmap)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance(uri: Uri) =
            PhotoFragment().apply {
                arguments = Bundle().apply {

                   putParcelable(ARG_URI,uri)
                }
            }
    }
}