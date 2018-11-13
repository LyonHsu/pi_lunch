package raspberrypiluncher.android.lyon.com.raspberrypiluncher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager.LayoutParams;

//import com.google.android.things.contrib.driver.button.Button;
//import com.google.android.things.contrib.driver.button.ButtonInputDriver;
//import com.google.android.things.pio.Gpio;
//import com.google.android.things.pio.PeripheralManagerService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.android.GsonFactory;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Metadata;
import ai.api.model.Result;
import ai.api.model.Status;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import raspberrypiluncher.android.lyon.com.raspberrypiluncher.wifi.WifiMenu;

public class MainActivity extends Activity implements AIListener {

    private TextView txtSpeechInput;
    private ImageButton btnSpeak;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    final OkHttpClient client = new OkHttpClient();
    private TextToSpeech textToSpeech;
    private String jokeString = "有一天小明帶著一把玩具鈔票代玩具店, 他挑了ㄧ架最大的玩具飛機, 然後到櫃台拿那把玩具鈔票給服務人員,   服務人員說 小弟弟這個是假的 不能結帳喔,   小明回嗆 那你的飛機就是真的嗎?  ";
    private MediaPlayer mp;
    private String musicString ="放音樂給你聽";

    private AIService aiService;
    private Gson gson = GsonFactory.getGson();
    public static final String TAG = "kevin";
//    private Gpio mLedGpio;
//    private ButtonInputDriver mButtonInputDriver;
    private String gpioLed = "BCM6";
    private String gpioButton = "BCM21";

    private float touchX;
    private float touchY;
    //private int   tvWidth  = getWindow().LayoutParams.WRAP_CONTENT;
    //private int   tvHeight = LayoutParams.WRAP_CONTENT;

    ImageButton setting;
    private String cityName= "";
    private String weatherSpeech="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);

        // hide the action bar
        //getActionBar().hide();

        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("kevin","btnSpeak ");
                promptSpeechInput();
            }
        });

        setting =(ImageButton)findViewById(R.id.setting);
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this,Setting.class);
                startActivity(i);
            }
        });

        btnSpeak.requestFocus();

        textToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.d("kevin", "TTS init status:" + status);
                if (status != TextToSpeech.ERROR) {
                    int result = textToSpeech.setLanguage(Locale.TAIWAN);//Locale.);

                    Log.d("kevin", "speak result:" + result);

                    result = textToSpeech.speak("請按鈕說出你要查的資料", TextToSpeech.QUEUE_FLUSH, null);

                    Log.d("kevin", "speak result:" + result);
                }
            }
        });
        mp = MediaPlayer.create(this,R.raw.sia);
        //cht 972e735a23a14da897054fe525085a42
        //eng 7cb7b5d9b32040bdbf2062109c86b94f
//		final AIConfiguration config = new AIConfiguration("972e735a23a14da897054fe525085a42",
//				AIConfiguration.SupportedLanguages.ChineseTaiwan,
//				AIConfiguration.RecognitionEngine.System);

        //aiService = AIService.getService(this, config);
        //aiService.setListener(this);

//		PeripheralManagerService pioService = new PeripheralManagerService();
//		try {
//			Log.i(TAG, "Configuring GPIO pins");
//			mLedGpio = pioService.openGpio(gpioLed);
//			mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
//
//			Log.i(TAG, "Registering button driver");
//			// Initialize and register the InputDriver that will emit SPACE key events
//			// on GPIO state changes.
//			mButtonInputDriver = new ButtonInputDriver(
//					gpioButton,
//					Button.LogicState.PRESSED_WHEN_LOW,
//					KeyEvent.KEYCODE_SPACE);
//			mButtonInputDriver.register();
//		} catch (IOException e) {
//			Log.e(TAG, "Error configuring GPIO pins", e);
//		}





        TextView ipAddress = (TextView)findViewById(R.id.ipAddress);
        ipAddress.setText( "" + getLocalIpAddress(this).replace("\n",", "));

    }



    @SuppressLint("WifiManagerLeak")
    public String getLocalIpAddress(Context context) {

        String ip =  "no connect wifi!";
        WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        int ipAddress = wifiInf.getIpAddress();
        ip=String.format("%d.%d.%d.%d", (ipAddress & 0xff),(ipAddress >> 8 & 0xff),(ipAddress >> 16 & 0xff),(ipAddress >> 24 & 0xff));

        Log.i(TAG, "***** IP="+ ip);


        return "Wifi:"+ip+"\n ("+wifiInf.getSSID().toString()+") connected\n"+wifiInf.getBSSID();
    }


    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        //aiService.startListening();

        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        if(mp != null)
        {
            mp.stop();
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.TAIWAN);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }



        //ApiAi("video");
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("kevin","onKeyDown keyCode = "+keyCode);
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn on the LED
            Log.d("kevin","onKeyDown");
            setLedValue(true);
            promptSpeechInput();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.d("kevin","onKeyUp keyCode = "+keyCode);
        if (keyCode == KeyEvent.KEYCODE_SPACE) {
            // Turn off the LED
            Log.d("kevin","onKeyUp");
            setLedValue(false);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    /**
     * Update the value of the LED output.
     */
    private void setLedValue(boolean value) {
//		try {
//			mLedGpio.setValue(value);
//		} catch (IOException e) {
//			Log.e(TAG, "Error updating GPIO value", e);
//		}
    }





    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtSpeechInput.setText(result.get(0));
                    Log.d("kevin","result"+result);
                    if("joke".equals(result.get(0).trim())||"笑話".equals(result.get(0).trim())) {
                        txtSpeechInput.setText(result.get(0)+"\n"+jokeString);
                        ttsResult(jokeString);
                    }else if("music".equals(result.get(0).trim())||"音樂".equals(result.get(0).trim())){
                        //ttsResult(musicString);
                        txtSpeechInput.setText(result.get(0)+"\n"+musicString);
                        try {
                            if (mp != null) {
                                try {
                                    mp.prepare();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                mp.start();
                            }
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                        //mp.start();
                    }
                    else if("現在時間".contains(result.get(0).trim())){
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss");

                        Date curDate = new Date(System.currentTimeMillis()) ; // 獲取當前時間

                        String str = formatter.format(curDate);

                        txtSpeechInput.setText(str);
                        ttsResult("現在時間"+str);
                    }
                    else {
                        //searchWiki(result.get(0));
                        ApiAi(result.get(0));
                    }
                }
                break;
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void searchWiki(String keywork){
        Request request = new Request.Builder()
                .url("http://signalr.tn.edu.tw/Owikipedia/api/abstract/"+keywork)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                String json = null;
                try {
                    json = response.body().string().trim();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("kevin", json.toString());
                //解析JSON
                //parseJSON(json);
                ttsResult(json);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                //告知使用者連線失敗
            }
        });
    }

    public void ApiAi(String keywork){
        RequestBody requestBody = new FormBody.Builder()
                .add("sessionId", "ed0eb921-5aa2-4394-90c9-a15e559a0e2b")
                .add("lang", "zh-tw")
                .add("query", "["+keywork+"]")
                .build();

        MediaType JSON=MediaType.parse("application/json;charset=UTF-8");

        String postBody =  "{\"query\":[\""
                + keywork
                + "\"],"
                + "\"name\":\"Bowling\","
                + "\"lang\": \"en\","
                + " \"sessionId\": \"ed0eb921-5aa2-4394-90c9-a15e559a0e2b\""
                + "}";
        RequestBody body = RequestBody.create(JSON, postBody);

        Log.d(TAG,"body"+requestBody.toString());

        Request request = new Request.Builder()
                .url("https://api.api.ai/v1/query?v=20150910")
                .header("Authorization","Bearer 972e735a23a14da897054fe525085a42")
                .post(body)
                .build();

        Log.d(TAG,request.toString());

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                String json = null;
                try {
                    json = response.body().string().trim();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("kevin", json.toString());
                //解析JSON
                AIResponse aiResponse = gson.fromJson(json, AIResponse.class);
                //parseJSON(json);
                //ttsResult(json);
                //txtSpeechInput.setText(json.toString());
                aiResult(aiResponse);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                //告知使用者連線失敗
            }
        });
    }

    public void searchWeather(String keywork){
        String cityId = "";
        cityId=checkCityId(keywork);
        Request request = new Request.Builder()
                .url("https://works.ioa.tw/weather/api/weathers/"+cityId+".json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                String json = null;
                try {
                    json = response.body().string().trim();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d("kevin", "city tamp= " +json.toString());
                //解析JSON
                //parseJSON(json);
                //txtSpeechInput.setText(json.toString());
                //ttsResult(json);
                WeatherResult(json);
            }
            @Override
            public void onFailure(Call call, IOException e) {
                //告知使用者連線失敗
            }
        });
    }

    public void WeatherResult(final String json) {
		/*
		Result result = response.getResult();

		// Get parameters
		String parameterString = "";
		if (result.getParameters() != null && !result.getParameters().isEmpty()) {
			for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
				parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
			}
		}

		// Show results in TextView.
		txtSpeechInput.setText("Query:" + result.getResolvedQuery() +
				"\nAction: " + result.getAction() +
				"\nParameters: " + parameterString);
		*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult");
                JSONObject reader=new JSONObject();
                try {
                    reader = new JSONObject(json);
                    txtSpeechInput.setText(gson.toJson(reader));
                    Log.d(TAG,"weather json:"+gson.toJson(reader));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //txtSpeechInput.setText(gson.toJson(reader));
                String temperature = reader.opt("temperature").toString();
                String desc = reader.opt("desc").toString();
                Log.d("kevin","temp/desc = "+temperature+"/"+desc);
                weatherSpeech=cityName+"溫度"+temperature+"度"+"天氣"+desc;
                ttsResult(weatherSpeech);
//				Log.i(TAG, "Received success response");
//
//				// this is example how to get different parts of result object
//				final Status status = reader.getStatus();
//				Log.i(TAG, "Status code: " + status.getCode());
//				Log.i(TAG, "Status type: " + status.getErrorType());
//
//				final Result result = json.getResult();
//				Log.i(TAG, "Resolved query: " + result.getResolvedQuery());
//
//				Log.i(TAG, "Action: " + result.getAction());
//
//				final String speech = result.getFulfillment().getSpeech();
//				Log.i(TAG, "Speech: " + speech);
//				if("open friday video app play movie".equals(speech)){
//					String url = "http://video.friday.tw/m/movieapp/detail/16002";
//					Intent i = new Intent(Intent.ACTION_VIEW);
//					i.setData(Uri.parse(url));
//					startActivity(i);
//				}else {
//					textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
//				}
//
//				final Metadata metadata = result.getMetadata();
//				if (metadata != null) {
//					Log.i(TAG, "Intent id: " + metadata.getIntentId());
//					Log.i(TAG, "Intent name: " + metadata.getIntentName());
//				}
//
//				final HashMap<String, JsonElement> params = result.getParameters();
//				if (params != null && !params.isEmpty()) {
//					Log.i(TAG, "Parameters: ");
//					for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
//						Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
//					}
//				}
            }

        });

    }

    public String checkCityId(String city_Name){
        String id ="";
        switch(city_Name){
            case "臺北市":
                id="1";
                break;
            case "基隆市":
                id="2";
                break;
            case "新北市":
                id="3";
                break;
            case "連江縣":
                id="4";
                break;
            case "宜蘭縣":
                id="5";
                break;
            case "新竹市":
                id="6";
                break;
            case "新竹縣":
                id="7";
                break;
            case "苗栗縣":
                id="9";
                break;
            case "臺中市":
                id="10";
                break;
            case "彰化縣":
                id="11";
                break;
            case "南投縣":
                id="12";
                break;
            case "嘉義市":
                id="13";
                break;
            case "嘉義縣":
                id="14";
                break;
            case "雲林縣":
                id="15";
                break;
            case "臺南市":
                id="16";
                break;
            case "高雄市":
                id="17";
                break;
            case "澎湖縣":
                id="18";
                break;
            case "金門縣":
                id="19";
                break;
            case "屏東縣":
                id="20";
                break;
            case "臺東縣":
                id="21";
                break;
            case "花蓮縣":
                id="22";
                break;
        }
        Log.i("kevin","return id = "+id);
        return id;
    }

    @Override
    protected void onDestroy(){
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
            textToSpeech = null;
        }
        if(mp != null)
        {
            mp.stop();
        }
        super.onDestroy();

//        if (mButtonInputDriver != null) {
//            mButtonInputDriver.unregister();
//            try {
//                mButtonInputDriver.close();
//            } catch (IOException e) {
//                Log.e(TAG, "Error closing Button driver", e);
//            } finally{
//                mButtonInputDriver = null;
//            }
//        }
//
//		if (mLedGpio != null) {
//			try {
//				mLedGpio.close();
//			} catch (IOException e) {
//				Log.e(TAG, "Error closing LED GPIO", e);
//			} finally{
//				mLedGpio = null;
//			}
//			mLedGpio = null;
//		}
    }

    public void ttsResult(String result){
        String text="";
        if("".equals(result) || result== null){
            text = "查詢不到資料";//"kevin1";
        }else{
            text = result;//"kevin1";
        }

        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        if(mp != null)
        {
            mp.stop();
        }
        Log.d("kevin","tts result"+text);
        //float ratepara = 0.5;
        //textToSpeech.setSpeechRate(ratepara);
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }


    public void aiResult(final AIResponse response) {
		/*
		Result result = response.getResult();

		// Get parameters
		String parameterString = "";
		if (result.getParameters() != null && !result.getParameters().isEmpty()) {
			for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
				parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
			}
		}

		// Show results in TextView.
		txtSpeechInput.setText("Query:" + result.getResolvedQuery() +
				"\nAction: " + result.getAction() +
				"\nParameters: " + parameterString);
		*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Result result = response.getResult();
                Log.d(TAG, "onResult" + response.getResult().toString());

                //txtSpeechInput.setText(response.toString());
                //setTxtSpeechInput(response.getResult().toString());

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());

                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);

                if ("open friday video play star wars".equals(speech)||"開啟friday影音app".equals(speech)||"開啟影音軟體".equals(speech)) {
                    try {
                        String url = "http://video.friday.tw/m/movieapp/detail/16356";
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        txtSpeechInput.setText(speech);
                        //http://video.friday.tw/movie/detail/16002
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "IOException:"+e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                }

                textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);


                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                        if("query.weather".equals(result.getAction())){
                            Log.d("kevin","key = "+entry.getKey());
                            if("taiwan-city".equals(entry.getKey())){
                                cityName= String.valueOf(entry.getValue());
                                searchWeather(cityName.replace("\"",""));
                                Log.i("kevin","cityname = "+cityName );
                            }
                        }
                        if("query.wiki".equals(result.getAction())){
                            Log.d("kevin","key = "+entry.getKey());
                            cityName= String.valueOf(entry.getValue()).trim();
                            searchWiki(cityName.replace("查詢",""));
                            Log.i("kevin","cityname = "+cityName );

                        }
                    }
                }
                if("query.wiki".equals(result.getAction())){
                    //cityName= String.valueOf(entry.getValue()).trim();
                    Log.d("kevin","wiki keyword = "+result.getResolvedQuery().replace("查詢","").trim());
                    searchWiki(result.getResolvedQuery().replace("查詢","").trim());
                }
            }
        });

    }

    @Override
    public void onResult(final AIResponse response) {
		/*
		Result result = response.getResult();

		// Get parameters
		String parameterString = "";
		if (result.getParameters() != null && !result.getParameters().isEmpty()) {
			for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
				parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
			}
		}

		// Show results in TextView.
		txtSpeechInput.setText("Query:" + result.getResolvedQuery() +
				"\nAction: " + result.getAction() +
				"\nParameters: " + parameterString);
		*/
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "onResult");

                txtSpeechInput.setText(gson.toJson(response));

                Log.i(TAG, "Received success response");

                // this is example how to get different parts of result object
                final Status status = response.getStatus();
                Log.i(TAG, "Status code: " + status.getCode());
                Log.i(TAG, "Status type: " + status.getErrorType());

                final Result result = response.getResult();
                Log.i(TAG, "Resolved query: " + result.getResolvedQuery());

                Log.i(TAG, "Action: " + result.getAction());

                final String speech = result.getFulfillment().getSpeech();
                Log.i(TAG, "Speech: " + speech);
                if("open friday video app play movie".equals(speech)){
                    String url = "http://video.friday.tw/m/movieapp/detail/16002";
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
                    startActivity(i);
                }else {
                    textToSpeech.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
                }

                final Metadata metadata = result.getMetadata();
                if (metadata != null) {
                    Log.i(TAG, "Intent id: " + metadata.getIntentId());
                    Log.i(TAG, "Intent name: " + metadata.getIntentName());
                }

                final HashMap<String, JsonElement> params = result.getParameters();
                if (params != null && !params.isEmpty()) {
                    Log.i(TAG, "Parameters: ");
                    for (final Map.Entry<String, JsonElement> entry : params.entrySet()) {
                        Log.i(TAG, String.format("%s: %s", entry.getKey(), entry.getValue().toString()));
                    }
                }
            }

        });

    }

    @Override
    public void onError(AIError error) {
        txtSpeechInput.setText(error.toString());
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    public void setTxtSpeechInput(String json){
        txtSpeechInput.setText(json);
    }


    @Override
    // 利用 MotionEvent 處理觸控程序
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("kevin","onTouchEvent");
        touchX = event.getX();       // 觸控的 X 軸位置
        touchY = event.getY() - 50;  // 觸控的 Y 軸位置

        // 判斷觸控動作
        switch( event.getAction() ) {

            case MotionEvent.ACTION_DOWN:  // 按下
                Log.d("kevin","MotionEvent.ACTION_DOWN");
                // 設定 TextView 內容, 大小, 位置
                txtSpeechInput.setText("X: " + touchX + ", Y: " + touchY + ", 按下");
				/*
				txtSpeechInput.setLayoutParams( new AbsoluteLayout.LayoutParams( tvWidth
						, tvHeight
						, (int)touchX
						, (int)touchY
				));
				*/
                break;

            case MotionEvent.ACTION_MOVE:  // 拖曳移動
                Log.d("kevin","MotionEvent.ACTION_MOVE");
                // 設定 TextView 內容, 大小, 位置
                txtSpeechInput.setText("X: " + touchX + ", Y: " + touchY + ", 拖曳移動");
				/*
				txtSpeechInput.setLayoutParams( new AbsoluteLayout.LayoutParams( tvWidth
						, tvHeight
						, (int)touchX
						, (int)touchY
				));
				*/
                break;

            case MotionEvent.ACTION_UP:  // 放開
                Log.d("kevin","MotionEvent.ACTION_UP");
                // 設定 TextView 內容
                txtSpeechInput.setText("X: " + touchX + ", Y: " + touchY + ", 放開");
                break;
        }

        // TODO Auto-generated method stub
        return super.onTouchEvent(event);
    }
}
