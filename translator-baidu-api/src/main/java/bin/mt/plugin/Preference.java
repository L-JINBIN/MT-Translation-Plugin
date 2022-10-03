package bin.mt.plugin;

import bin.mt.plugin.api.MTPluginContext;
import bin.mt.plugin.api.preference.PluginPreference;

public class Preference implements PluginPreference {

    @Override
    public void onBuild(MTPluginContext context, Builder builder) {
        builder.setLocalString(context.getLocalString());

        // 百度
        builder.addHeader("{plugin_name}");

        builder.addInput("APP ID", BaiduConstant.BAIDU_APPID_PREFERENCE_KEY)
                .defaultValue(BaiduConstant.BAIDU_APPID_DEFAULT)
                .summary("{bt_appid_summary}");

        builder.addInput("Key", BaiduConstant.BAIDU_KEY_PREFERENCE_KEY)
                .defaultValue(BaiduConstant.BAIDU_KEY_DEFAULT)
                .summary("{bt_key_summary}");

        builder.addText("{reg_url}")
                .summary("{reg_url_summary}")
                .url("https://api.fanyi.baidu.com");
    }

}
