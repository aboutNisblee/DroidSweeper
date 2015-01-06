/*
 * msm-jni.hpp
 *
 *  Created on: 19.11.2012
 *      Author: moritz
 */

#ifndef MSM_JNI_HPP_
#define MSM_JNI_HPP_

#define JAVA(fn) Java_de_nisble_droidsweeper_game_jni_MineSweeperMatrix_##fn

#include <jni.h>

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM*, void*);
JNIEXPORT void JNICALL JAVA(init)(JNIEnv* env, jobject thiz);
JNIEXPORT void JNICALL JAVA(free)(JNIEnv*, jobject);
JNIEXPORT void JNICALL JAVA(nativeCreate)(JNIEnv*, jobject, jint, jint, jint);
JNIEXPORT void JNICALL JAVA(nativeSetFieldListener)(JNIEnv*, jobject, jobject, jint, jint);
JNIEXPORT jint JNICALL JAVA(nativeGameStatus)(JNIEnv*, jobject);
JNIEXPORT jint JNICALL JAVA(nativeRemainingBombs)(JNIEnv*, jobject);
JNIEXPORT jint JNICALL JAVA(nativeReveal)(JNIEnv*, jobject, jint, jint);
JNIEXPORT void JNICALL JAVA(nativeCycleMark)(JNIEnv*, jobject, jint, jint);

#ifdef __cplusplus
}
#endif
#endif /* MSM_JNI_HPP_ */
