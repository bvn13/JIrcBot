ALTER TABLE public.channel_settings DROP CONSTRAINT uniq_channel_settings_server_host_channel_name;

ALTER TABLE public.channel_settings RENAME dtcreated  TO dt_created;
ALTER TABLE public.channel_settings RENAME dtupdated  TO dt_updated;
ALTER TABLE public.channel_settings RENAME advicesenabled  TO advices_enabled;
ALTER TABLE public.channel_settings RENAME autorejoinenabled  TO auto_rejoin_enabled;
ALTER TABLE public.channel_settings RENAME bashorgenabled  TO bash_org_enabled;
ALTER TABLE public.channel_settings RENAME calculatorenabled  TO calculator_enabled;
ALTER TABLE public.channel_settings RENAME channelname  TO channel_name;
ALTER TABLE public.channel_settings RENAME deferredmessagesenabled  TO deferred_messages_enabled;
ALTER TABLE public.channel_settings RENAME helloonjoinenabled  TO hello_on_join_enabled;
ALTER TABLE public.channel_settings RENAME linkpreviewenabled  TO link_preview_enabled;
ALTER TABLE public.channel_settings RENAME quizenabled  TO quiz_enabled;
ALTER TABLE public.channel_settings RENAME regexcheckerenabled  TO regex_checker_enabled;
ALTER TABLE public.channel_settings RENAME grammarcorrectionenabled  TO grammar_correction_enabled;
ALTER TABLE public.channel_settings RENAME googlesearchenabled  TO google_search_enabled;
ALTER TABLE public.channel_settings RENAME loggingenabled  TO logging_enabled;
ALTER TABLE public.channel_settings RENAME onjoinmessage  TO on_join_message;
ALTER TABLE public.channel_settings RENAME serverhost  TO server_host;


ALTER TABLE public.deferred_messages RENAME dtcreated  TO dt_created;
ALTER TABLE public.deferred_messages RENAME dtupdated  TO dt_updated;
ALTER TABLE public.deferred_messages RENAME channelname  TO  channel_name;


ALTER TABLE public.irc_messages RENAME dtcreated  TO dt_created;
ALTER TABLE public.irc_messages RENAME dtupdated  TO dt_updated;
ALTER TABLE public.irc_messages RENAME channelname  TO channel_name;
ALTER TABLE public.irc_messages RENAME serverhost  TO server_host;


ALTER TABLE public.grammar_correction RENAME dtcreated  TO dt_created;
ALTER TABLE public.grammar_correction RENAME dtupdated  TO dt_updated;
