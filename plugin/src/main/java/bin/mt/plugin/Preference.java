package bin.mt.plugin;

import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Preference implements PluginPreference {

    @Override
    public void onBuild(MTPluginContext context, Builder builder) {
        builder.setLocalString(context.getLocalString());

        // 百度
        builder.addHeader("{bt}");

        builder.addInput("APP ID", Constant.BAIDU_APPID_PREFERENCE_KEY)
                .defaultValue(Constant.BAIDU_APPID_DEFAULT)
                .summary("{bt_appid_summary}");

        builder.addInput("Key", Constant.BAIDU_KEY_PREFERENCE_KEY)
                .defaultValue(Constant.BAIDU_KEY_DEFAULT)
                .summary("{bt_key_summary}");

        builder.addText("{reg_url}")
                .summary("{bt_reg_url_summary}")
                .url("https://api.fanyi.baidu.com");

        // Yandex
        builder.addHeader("{yt}");

        builder.addInput("API Key", Constant.YANDEX_KEY_PREFERENCE_KEY)
                .defaultValue(Constant.YANDEX_KEY_DEFAULT)
                .summary("{yt_key_summary}");

        builder.addText("{reg_url}")
                .summary("{yt_reg_url_summary}")
                .url("https://tech.yandex.com/translate");
    }

}
