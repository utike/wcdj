#include "Sample1.h"
#include <string.h>

JNIEXPORT jint JNICALL Java_Sample1_intMethod
 (JNIEnv *env, jobject obj, jint num) {
  return num * num;
}

JNIEXPORT jboolean JNICALL Java_Sample1_booleanMethod
  (JNIEnv *env, jobject obj, jboolean boolean) {
  return !boolean;
}


JNIEXPORT jstring JNICALL Java_Sample1_stringMethod
  (JNIEnv *env, jobject obj, jstring string) {
    const char *str = env->GetStringUTFChars(string, 0);
    char cap[128];
    strcpy(cap, str);
    env->ReleaseStringUTFChars(string, str);

    // NOTE:
    // compile error:
    // java: symbol lookup error: /data/home/gerryyang/test/Java/jni_java_call_c/jni_c_2/libSample1.so: undefined symbol: strupr
    //
    // https://recalll.co/ask/v/topic/java-jni-undefined-symbol-error/555b06c72bd273565c8bdff6
    // strupr is not standard ANSI C. If you write a native C application that references strupr, you will get a link error similar to what you are seeing
    //return env->NewStringUTF(strupr(cap));

    return env->NewStringUTF(cap);
}

JNIEXPORT jint JNICALL Java_Sample1_intArrayMethod
  (JNIEnv *env, jobject obj, jintArray array) {
    int i, sum = 0;
    jsize len = env->GetArrayLength(array);
    jint *body = env->GetIntArrayElements(array, 0);
    for (i=0; i<len; i++)
    {   sum += body[i];
    }
    env->ReleaseIntArrayElements(array, body, 0);
    return sum;
}

int main(){}