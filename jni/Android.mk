
include $(call all-subdir-makefiles)

include $(CLEAR_VARS)
LOCAL_PATH := /home/zokama/Prog/Workspace/AndroLirc/jni
LOCAL_C_INCLUDES := lirc

LOCAL_MODULE    := androlirc
LOCAL_SRC_FILES := androlirc.c\
                lirc/daemons/ir_remote.c\
                lirc/daemons/hw-types.c\
                lirc/daemons/config_file.c\
                lirc/daemons/receive.c\
                lirc/daemons/release.c\
                lirc/daemons/input_map.c\
                lirc/daemons/dump_config.c\
                lirc/daemons/irrecord.c\
                lirc/daemons/hw_default.c\
                lirc/daemons/transmit.c


#LOCAL_STATIC_LIBRARIES := lircd
LOCAL_LDLIBS := -llog
LOCAL_CFLAGS := -DHAVE_CONFIG_H

include $(BUILD_SHARED_LIBRARY)
