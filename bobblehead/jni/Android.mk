LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := imageprocessing
LOCAL_SRC_FILES := imageprocessing.c
LOCAL_LDLIBS    := -lm -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)

The application uses Java and C/C++ to accomplish the desired features described earlier. Java was the most heavily used programming language in the design of the application. It was used to implement all features with the exception of the image processing.  
The Java code is split into three activities: HomeScreenActivity, FaceSelectActivity and BobActivity.  On the launch of the application, HomeScreenActivity begins.  This activity uses a relative layout to display our logo and two buttons prompting the user to either select a photo from the android gallery or capture a new photo using the built in camera.  If the user selects to choose a picture from the gallery, a new intent is created which opens the android gallery activity which allows the user to select a photo. On the activity result, the photo is saved to a temporary file and its location passed to the FaceSelectActivity  The same happens when the user selects to take a new picture, but the intent calls the Andoid camera activity. 
The FaceSelectActivity prompts the user to select a face by enlarging an oval over the region.  Once they are happy with the selected region, the user hits the go button.  This button calls a few java methods that crop the selected image into a rectangle, makes the area around the oval clear and stores it in a Bitmap. This bitmap is then stored to a temporary location where  BobActivity will use it as well as the original background to display the anima.  
BobActivity then opens the Bitmap and sends it to the native interface where a C function applies a fisheye effect. Then, this Bitmap is put into a face class. The face class controls the position and rotational angle of the bitmap. The BobActivity’s event sensors (gravity, accelerometer, and touch) then vary the position and rotation of the face based on Face.computePhysics. This all creates the effect of the face bobbling on top of the original Bitmap