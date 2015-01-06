/*
 * msm-jni.cpp
 *
 *  Created on: 19.11.2012
 *      Author: moritz
 */

#include "msm-jni.hpp"

#include <string>
#include <sstream>
#include <android/log.h>
#define  LOG_TAG    "msm-jni"
#if NDEBUG
#define  LOGV(...)
#define  LOGD(...)
#define  LOGI(...)
#else
#define  LOGV(...)  __android_log_print(ANDROID_LOG_VERBOSE,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#endif
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARNING,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define EX_OOB "java/lang/IndexOutOfBoundsException"
#define EX_NP "java/lang/NullPointerException"

#define JAVA_EXCEPTION(ex_str, message) java_exception(__FILE__,__LINE__, ex_str, message)

#include "matrix.hpp"

namespace
{

JavaVM* cached_vm;

// GlobalRef to out of bounds exception
jclass jcls_oob;
// GlobalRef to null pointer exception
jclass jcls_np;

// GlobalRef to the GameStatus enum class
jclass jcls_GameStatus;
// MethodID if the fromInt method of GameStatus
jmethodID method_GameStatus_fromInt;

// GlobalRef to the FieldStatus enum class
jclass jcls_FieldStatus;
// MethodID if the fromInt method of FieldStatus
jmethodID method_FieldStatus_fromInt;

jobject jobj_MineSweeperMatrix;
jmethodID method_MineSweeperMatrix_onGameStatusChanged;
jmethodID method_MineSweeperMatrix_onRemainingBombsChanged;
jmethodID method_MineSweeperMatrix_afterFieldStatusChanged;

msm::Matrix matrix;

void java_exception(const char* file, int line, const char* exception, const char* msg)
{
	JNIEnv* env;
	jclass jcls_ex = 0;

	cached_vm->AttachCurrentThread(&env, NULL);
	if (0 == env)
	{
		LOGE("Unable to attach current thread");
		return;
	}

	jcls_ex = env->FindClass(exception);
	if (0 == jcls_ex)
	{
		LOGE("Unable to find exception class: %s", exception);
		return;
	}

	std::stringstream ss;
	ss << "JNIException in " << file << " at " << line << ": " << msg;
	env->ThrowNew(jcls_ex, ss.str().data());
	env->DeleteLocalRef(jcls_ex);
}

struct MatrixHandler: public msm::MatrixObserver
{
	void onGameStatusChanged(msm::Matrix const& /* matrix */, msm::GAMESTATUS newStatus)
	{
		LOGD("Gamestatus changed to %s", msm::toString(newStatus));

		JNIEnv* env;
		cached_vm->AttachCurrentThread(&env, NULL);
		if (0 == env)
		{
			LOGE("Unable to attach current thread");
			return;
		}

		jobject gamestatus = env->CallStaticObjectMethod(jcls_GameStatus, method_GameStatus_fromInt, (jint) newStatus);
		if (0 == gamestatus)
		{
			LOGE("Unable to create game status object");
			return;
		}

		// Call onGameStatusChanged() in the binding
		env->CallVoidMethod(jobj_MineSweeperMatrix, method_MineSweeperMatrix_onGameStatusChanged, gamestatus);
	}
	void onRemainingBombsChanged(msm::Matrix const& /* matrix */, int32_t remainingBombs)
	{
		LOGD("Remaining bombs: %d", remainingBombs);

		JNIEnv* env;
		cached_vm->AttachCurrentThread(&env, NULL);
		if (0 == env)
		{
			LOGE("Unable to attach current thread");
			return;
		}

		// Call onRemainingBombsChanged() in the binding
		env->CallVoidMethod(jobj_MineSweeperMatrix, method_MineSweeperMatrix_onRemainingBombsChanged,
				(jint) remainingBombs);
	}
	void onFieldStatusChanged(msm::Matrix const& /* matrix */, msm::Field const& field, msm::FIELDSTATUS newStatus)
	{
		LOGD("Status of X:%d Y:%d changed to %s", field.getPosition().X, field.getPosition().Y,
				msm::toString(newStatus));

		jobject obj = (jobject) field.getJavaRef();
		if (0 == obj)
		{
			JAVA_EXCEPTION(EX_NP, "No object ref configured for this field");
			return;
		}

		jmethodID met = (jmethodID) field.getJavaMethodID();
		if (0 == met)
		{
			JAVA_EXCEPTION(EX_NP, "No method configured for this field");
			return;
		}

		JNIEnv* env;
		cached_vm->AttachCurrentThread(&env, NULL);
		if (0 == env)
		{
			LOGE("Unable to attach current thread");
			return;
		}

		jobject fieldstatus = env->CallStaticObjectMethod(jcls_FieldStatus, method_FieldStatus_fromInt,
				(jint) newStatus);
		if (0 == fieldstatus)
		{
			LOGE("Unable to create field status object");
			return;
		}

		// Call the method stored in the field
		env->CallVoidMethod(obj, met, fieldstatus, field.getAdjacentBombs());

		// Call afterFieldStatusChanged() in the binding
		env->CallVoidMethod(jobj_MineSweeperMatrix, method_MineSweeperMatrix_afterFieldStatusChanged,
				field.getPosition().X, field.getPosition().Y, field.getAdjacentBombs(), fieldstatus);
	}

	void onFieldDelete(msm::Matrix const& /* matrix */, msm::Field const& field)
	{
		LOGD("Deleting X:%d Y:%d", field.getPosition().X, field.getPosition().Y);

		JNIEnv* env;
		cached_vm->AttachCurrentThread(&env, NULL);
		if (NULL == env)
		{
			LOGE("Unable to attach current thread");
			return;
		}

		env->DeleteGlobalRef((jobject) (field.getJavaRef()));
	}
} matrixHandler;

}

#ifdef __cplusplus
extern "C"
{
#endif

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved)
{
	cached_vm = vm;
	return JNI_VERSION_1_2;
}

JNIEXPORT void JNICALL JAVA(init)(JNIEnv* env, jobject thiz)
{
	LOGI("Initializing libmsm");

	matrix.addObserver(&matrixHandler);

	// Cache classes
	jclass tmp;

	tmp = env->FindClass("java/lang/IndexOutOfBoundsException");
	jcls_oob = (jclass) env->NewGlobalRef(tmp);

	tmp = env->FindClass("java/lang/NullPointerException");
	jcls_np = (jclass) env->NewGlobalRef(tmp);

	tmp = env->FindClass("de/nisble/droidsweeper/game/jni/GameStatus");
	if (0 == tmp)
	{
		LOGE("Unable to find de/nisble/droidsweeper/game/jni/GameStatus");
		env->DeleteLocalRef(tmp);
		return;
	}
	jcls_GameStatus = (jclass) env->NewGlobalRef(tmp);

	tmp = env->FindClass("de/nisble/droidsweeper/game/jni/FieldStatus");
	if (0 == tmp)
	{
		LOGE("Unable to find de/nisble/droidsweeper/game/jni/FieldStatus");
		env->DeleteLocalRef(tmp);
		return;
	}
	jcls_FieldStatus = (jclass) env->NewGlobalRef(tmp);

	method_GameStatus_fromInt = env->GetStaticMethodID(jcls_GameStatus, "fromInt",
			"(I)Lde/nisble/droidsweeper/game/jni/GameStatus;");
	if (0 == method_GameStatus_fromInt)
	{
		LOGE("Unable to find static getter of de/nisble/droidsweeper/game/jni/GameStatus");
		env->DeleteLocalRef(tmp);
		return;
	}

	method_FieldStatus_fromInt = env->GetStaticMethodID(jcls_FieldStatus, "fromInt",
			"(I)Lde/nisble/droidsweeper/game/jni/FieldStatus;");
	if (0 == method_FieldStatus_fromInt)
	{
		LOGE("Unable to find static getter of de/nisble/droidsweeper/game/jni/FieldStatus");
		env->DeleteLocalRef(tmp);
		return;
	}

	method_MineSweeperMatrix_onGameStatusChanged = env->GetMethodID(env->GetObjectClass(thiz), "onGameStatusChanged",
			"(Lde/nisble/droidsweeper/game/jni/GameStatus;)V");
	if (0 == method_MineSweeperMatrix_onGameStatusChanged)
	{
		LOGE("Unable to find onGameStatusChanged() function definition");
		env->DeleteLocalRef(tmp);
		return;
	}

	method_MineSweeperMatrix_onRemainingBombsChanged = env->GetMethodID(env->GetObjectClass(thiz),
			"onRemainingBombsChanged", "(I)V");
	if (0 == method_MineSweeperMatrix_onRemainingBombsChanged)
	{
		LOGE("Unable to find onRemainingBombsChanged() function definition");
		env->DeleteLocalRef(tmp);
		return;
	}

	method_MineSweeperMatrix_afterFieldStatusChanged = env->GetMethodID(env->GetObjectClass(thiz),
			"afterFieldStatusChanged", "(IIILde/nisble/droidsweeper/game/jni/FieldStatus;)V");
	if (0 == method_MineSweeperMatrix_afterFieldStatusChanged)
	{
		LOGE("Unable to find afterFieldStatusChanged() function definition");
		env->DeleteLocalRef(tmp);
		return;
	}

	// This should be safe since we can only be called by binding class
	jobj_MineSweeperMatrix = (jobject) env->NewGlobalRef(thiz);

	env->DeleteLocalRef(tmp);
}

JNIEXPORT void JNICALL JAVA(free)(JNIEnv* env, jobject thiz)
{
	LOGD("Freeing libmsm");

	env->DeleteGlobalRef(jcls_oob);
	env->DeleteGlobalRef(jcls_np);
	env->DeleteGlobalRef(jcls_FieldStatus);
	env->DeleteGlobalRef(jcls_GameStatus);
	env->DeleteGlobalRef(jobj_MineSweeperMatrix);
}

JNIEXPORT void JNICALL JAVA(nativeCreate)(JNIEnv* env, jobject thiz, jint size_x, jint size_y, jint bombs)
{
	LOGD("Creating new matrix. Dimensions: X:%d Y:%d B:%d", size_x, size_y, bombs);

	matrix.reset(msm::Dimensions(size_x, size_y, bombs));
}

JNIEXPORT void JNICALL JAVA(nativeSetFieldListener)(JNIEnv* env, jobject thiz, jobject obj, jint x, jint y)
{
	LOGV("Registering callback for field X:%d Y:%d", x, y);

	jobject ref = env->NewGlobalRef(obj);
	if (env->ExceptionCheck())
		return;

	jclass cls = env->GetObjectClass(obj);
	jmethodID met = env->GetMethodID(cls, "onStatusChanged", "(Lde/nisble/droidsweeper/game/jni/FieldStatus;I)V");
	if (env->ExceptionCheck())
		return;

	try
	{
		matrix[x][y].setJavaRef((void*) ref);
		matrix[x][y].setJavaMethodID((void*) met);
	} catch (msm::IndexOutOfBoundsException const& ex)
	{
		env->DeleteGlobalRef(ref);

		LOGE("IndexOutOfBoundsException: %s", ex.what());
		JAVA_EXCEPTION(EX_OOB, ex.what());
	}
}

JNIEXPORT jint JNICALL JAVA(nativeGameStatus)(JNIEnv*, jobject)
{
	return matrix.getStatus();
}

JNIEXPORT jint JNICALL JAVA(nativeRemainingBombs)(JNIEnv*, jobject)
{
	return matrix.getRemainingBombs();
}

JNIEXPORT jint JNICALL JAVA(nativeReveal)(JNIEnv* env, jobject thiz, jint x, jint y)
{
	LOGD("Revealing field X:%d Y:%d", x, y);

	try
	{
		return matrix[x][y].reveal();
	} catch (msm::IndexOutOfBoundsException const& ex)
	{
		LOGE("IndexOutOfBoundsException: %s", ex.what());
		JAVA_EXCEPTION(EX_OOB, ex.what());
		// Return value is ignored by JVM because of set exception
		return 0;
	}
}

JNIEXPORT void JNICALL JAVA(nativeCycleMark)(JNIEnv* env, jobject thiz, jint x, jint y)
{
	LOGD("Cycle field mark X:%d Y:%d", x, y);

	try
	{
		matrix[x][y].cycleMark();
	} catch (msm::IndexOutOfBoundsException const& ex)
	{
		LOGE("IndexOutOfBoundsException: %s", ex.what());
		JAVA_EXCEPTION(EX_OOB, ex.what());
	}
}

#ifdef __cplusplus
}
#endif
