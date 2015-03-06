#include <jni.h>
#include <opencv2/opencv.hpp>
#include "opencv2/highgui/highgui.hpp"
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <vector>

#include "com_research_bretl_robovidd_MainActivity.h"

using namespace cv;

JNIEXPORT void JNICALL Java_com_research_bretl_robovidd_MainActivity_decode_1mat
  (JNIEnv *env, jobject, jbyteArray compressed_buff, jlong mat_addr, jint size) {
      jbyte* _compressed_buff= env->GetByteArrayElements(compressed_buff, 0);
      Mat *frame = (Mat *) mat_addr;
      int ptr = 0;
      for (int i = 0; i < .45 * 480; i++) {
          for (int j = 0; j < .45 * 640; j++) {
              frame->at <uchar>(i,j) = (uchar) _compressed_buff[ptr];
              ptr++;
          }
      }
      
      env->ReleaseByteArrayElements(compressed_buff, _compressed_buff, 0);
  }
