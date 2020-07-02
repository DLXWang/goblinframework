package org.goblinframework.rpc.module.converter;

import org.goblinframework.api.annotation.Install;
import org.goblinframework.core.util.HttpUtils;
import org.goblinframework.core.util.NumberUtils;
import org.goblinframework.rpc.registry.RpcServerNode;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;

import java.util.LinkedHashMap;

@Install
public class StringToRpcServerNodeConverter implements Converter<String, RpcServerNode> {

  @NotNull
  @Override
  public RpcServerNode convert(@NotNull String source) {
    LinkedHashMap<String, String> map = HttpUtils.parseQueryString(source);
    RpcServerNode node = new RpcServerNode();
    node.setServerId(map.get("serverId"));
    node.setServerName(map.get("serverName"));
    node.setServerHost(map.get("serverHost"));
    node.setServerPort(NumberUtils.toInt(map.get("serverPort")));
    node.setServerVersion(map.get("serverVersion"));
    node.setServerDomain(map.get("serverDomain"));
    node.setServerWeight(NumberUtils.toInt(map.get("serverWeight")));
    return node;
  }
}
