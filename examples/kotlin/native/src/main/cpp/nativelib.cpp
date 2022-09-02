#include <jni.h>
#include <string>

// Reference: https://stackoverflow.com/a/41820336/10708414
std::string jstring_to_string(JNIEnv *env, jstring jStr) {
    if (!jStr) return "";

    jclass stringClass = env->GetObjectClass(jStr);
    jmethodID getBytes = env->GetMethodID(stringClass, "getBytes", "(Ljava/lang/String;)[B");
    auto stringJbytes = (jbyteArray) env->CallObjectMethod(jStr, getBytes, env->NewStringUTF("UTF-8"));

    auto length = (size_t) env->GetArrayLength(stringJbytes);
    jbyte* pBytes = env->GetByteArrayElements(stringJbytes, nullptr);

    std::string ret = std::string((char *)pBytes, length);
    env->ReleaseByteArrayElements(stringJbytes, pBytes, JNI_ABORT);

    env->DeleteLocalRef(stringJbytes);
    env->DeleteLocalRef(stringClass);
    return ret;
}

std::string get_current_abi() {
#if defined(__arm__)
    return "arm";
#elif defined(__aarch64__)
    return "arm64";
#elif defined(__i386__)
    return "x86";
#elif defined(__x86_64__)
    return "x86_64";
#endif
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_giacomoferretti_odexpatcher_example_nativelib_MainActivity_getRunningAbi(
        JNIEnv *env,
        jobject /* this */) {
    return env->NewStringUTF(get_current_abi().c_str());
}

// Reference: https://stackoverflow.com/a/61163994/10708414
extern "C" JNIEXPORT jstring JNICALL
Java_com_giacomoferretti_odexpatcher_example_nativelib_MainActivity_getBuildConfigString(
        JNIEnv *env,
        jobject /* this */,
        jstring name) {
    jclass BuildConfig = env->FindClass("com/giacomoferretti/odexpatcher/example/nativelib/BuildConfig");
    jfieldID BuildConfigField = env->GetStaticFieldID(BuildConfig, jstring_to_string(env, name).c_str(), "Ljava/lang/String;");
    auto BuildConfigValue = (jstring) env->GetStaticObjectField(BuildConfig, BuildConfigField);

    const char *value = env->GetStringUTFChars(BuildConfigValue, nullptr);
    _jstring *ret = (env)->NewStringUTF(value);
    env->ReleaseStringUTFChars(BuildConfigValue, value);

    return ret;
}