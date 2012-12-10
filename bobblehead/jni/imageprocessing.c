#include <jni.h>
#include <time.h>
#include <android/log.h>
#include <android/bitmap.h>

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define  LOG_TAG    "libimageprocessing"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

static int rgb_clamp(int value) {
    if(value > 255) {
        return 255;
    }
    if(value < 0) {
        return 0;
    }
    return value;
}

static void brightness(AndroidBitmapInfo* info, void* pixels, float brightnessValue){
	uint32_t* pix;
    float h = info->height;
    float w = info->width;
    // for each row
    int y = 0;
    for (y;y<h;y++){
        // normalize y coordinate to -1 ... 1
        double ny = ((2*y)/h)-1;
        // pre calculate ny*ny
        double ny2 = ny*ny;
        // for each column
        int x = 0;
        for (x;x<w;x++) {
            pix = (uint32_t*)pixels;
            
            // normalize x coordinate to -1 ... 1
            double nx = ((2*x)/w)-1;
            // pre calculate nx*nx
            
            double nx2 = nx*nx;
            
            
            // calculate distance from center (0,0)
            // this will include circle or ellipse shape portion
            // of the image, depending on image dimensions
            // you can experiment with images with different dimensions
            double r = sqrt(nx2+ny2);
            // discard pixels outside from circle!
            if (0.0<=r&&r<=1.0){
                double nr = sqrt(1.0-r*r);
                // new distance is between 0 ... 1
                nr = (r + (1.0-nr)) / 2.0;
                // discard radius greater than 1.0
                if (nr<=1.0){
                    // calculate the angle for polar coordinates
                    double theta = atan2(ny,nx);
                    // calculate new x position with new distance in same angle
                    double nxn = nr*cos(theta);
                    // calculate new y position with new distance in same angle
                    double nyn = nr*sin(theta);
                    // map from -1 ... 1 to image coordinates
                    int x2 = (int)(((nxn+1)*w)/2.0);
                    // map from -1 ... 1 to image coordinates
                    int y2 = (int)(((nyn+1)*h)/2.0);
                    // find (x2,y2) position from source pixels
                    int srcpos = (int)(y2*w+x2);
                    // make sure that position stays within arrays
                    if (srcpos>=0 & srcpos < w*h){
                        // get new pixel (x2,y2) and put it to target array at (x,y)

                        pix[(int)(y*w+x)] = pix[srcpos];
                    }
                }
            }
        }
    }

    

}


JNIEXPORT void JNICALL Java_com_bn_bobblehead_BobActivity_brightness(JNIEnv * env, jobject  obj, jobject bitmap, jfloat brightnessValue)
{
    
    AndroidBitmapInfo  info;
    int ret;
    void* pixels;
    
    if ((ret = AndroidBitmap_getInfo(env, bitmap, &info)) < 0) {
        LOGE("AndroidBitmap_getInfo() failed ! error=%d", ret);
        return;
    }
    if (info.format != ANDROID_BITMAP_FORMAT_RGBA_8888) {
        LOGE("Bitmap format is not RGBA_8888 !");
        return;
    }
    
    if ((ret = AndroidBitmap_lockPixels(env, bitmap, &pixels)) < 0) {
        LOGE("AndroidBitmap_lockPixels() failed ! error=%d", ret);
    }
    
    brightness(&info,pixels, brightnessValue);
    
    AndroidBitmap_unlockPixels(env, bitmap);
}