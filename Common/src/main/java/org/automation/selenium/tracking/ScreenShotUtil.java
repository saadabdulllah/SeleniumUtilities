package org.automation.selenium.tracking;


import org.apache.commons.io.FileUtils;

import org.automation.selenium.core.SeleniumUtilBase;
import org.automation.selenium.property.PropertyUtil;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.shooting.ShootingStrategies;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by shantonu on 4/19/16.
 */
public class ScreenShotUtil extends SeleniumUtilBase {

    public static String ScreenPath = PropertyUtil.getUserDir()+ConfigHelper.screenshotImagerFolder;//PropertyUtil.getSysProperty("screenshot.folder");

    public byte[] takeScreenShotAsByteArray(){
        return ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.BYTES);

    }
    public String takeScreenShotAsString(){
        return ((TakesScreenshot) this.driver).getScreenshotAs(OutputType.BASE64);

    }
    public ScreenShotUtil(WebDriver aDriver) {
        super(aDriver);
    }

    /**
     * This is default screenshot using webdriver
     * @param name
     * @param isError
     * @return
     */
    public ScreenShotUtil takeScreenShot(String name, boolean isError){
        String imageName = getFileName(name,isError);
        StringBuilder fileNama = new StringBuilder(imageName);
        File screenShot = new File(fileNama.toString());
        screenShot.getParentFile().mkdir();
        new PageUtil(this.driver).waitForPageLoad();
        if(!new PageUtil(this.driver).isPageLoaded()){
            File srcFile = ((TakesScreenshot) executor).getScreenshotAs(OutputType.FILE);
            try{
                FileUtils.copyFile(srcFile,screenShot);PageUtil
            } catch (IOException e) {
                //todo default exception managemebt
            }
        }else
        {
            screenShotByJS(imageName);
        }
        return this;
    }


    /**
     * This is fully based on JS library used by project.. 100% project specific. use different library if you need to
     * Todo => hafl done, need to complete
     * main source => https://github.com/niklasvh/html2canvas
     */

    private ScreenShotUtil screenShotByJS(String imageName){
        String screenshotjs;
        String base64js_getImage;
        String base64_imageContant;
//todo testing => http://stackoverflow.com/questions/31656689/how-to-save-img-to-users-local-computer-using-html2canvas

        String script = " $('#save_image_locally').click(function(){\n" +
                "    html2canvas($('#imagesave'), \n" +
                "    {\n" +
                "      onrendered: function (canvas) {\n" +
                "        var a = document.createElement('a');\n" +
                "        a.href = canvas.toDataURL(\"image/jpeg\").replace(\"image/jpeg\", \"image/octet-stream\");\n" +
                "        a.download = '"+imageName+"';\n" +
                "        a.click();\n" +
                "      }\n" +
                "    });\n" +
                "  });";


        try{
        screenshotjs = new SourceUtil(this.driver).readJsLibrary("/js/html2canvas.js");
        /**The main JS => Change this if it does not works
         * function injectHtml2Canvas()
         {
         {
         %s
         }
         }
         var script = document.createElement('script');
         script.appendChild(document.createTextNode('('+ injectHtml2Canvas +')();'));
         (document.body || document.head || document.documentElement).appendChild(script);
         */
        base64js_getImage = String.format("function injectHtml2Canvas()\n                "
                +"{{\n%s\n}}\n \n                "
                +"var script = document.createElement(\'script\');\n                "
                +"script.appendChild(document.createTextNode(\'(\'+ injectHtml2Canvas +\')();\'));\n                "
                +"(document.body || document.head || document.documentElement).appendChild(script);", new Object[]{screenshotjs});
        base64_imageContant =  "return window.html2canvas != undefined;";

        boolean isSuccess = ((Boolean) executor.executeScript(base64_imageContant,new Object[0])).booleanValue();
        if(!isSuccess){ executor.executeScript(base64js_getImage, new Object[0]);
        }
        }catch (Exception e){

        }
        return this;
    }
    private String getFileName(String testname, boolean isError){
        SimpleDateFormat date = ConfigHelper.dateFormat;
        SimpleDateFormat time  = ConfigHelper.timeFormat;
        StringBuilder file = new StringBuilder();
        Date screenDate = new Date();
        
        if(ScreenPath!=null){
            file.append(ScreenPath).append(PropertyUtil.getSeperator());
        }
        file.append(PropertyUtil.getUserDir());
        file.append(PropertyUtil.getSeperator())
                .append("target")
                .append(PropertyUtil.getSeperator())
                .append(date.format(screenDate));
        if(isError){
            file.append(PropertyUtil.getSeperator()).append("error_");
        }else
        {
            file.append(PropertyUtil.getSeperator()).append("screenshot_");
        }
        file.append(testname+"_").append(time.format(screenDate));
        file = FileUtilities.trimLimit(file);
        file.append(ConfigHelper.screenshotType);
        return file.toString();
    }

    private ScreenShotUtil saveImage(String name, BufferedImage image){
        File output = new File(ScreenPath+name+"."+ConfigHelper.screenshotType);
        try {
            ImageIO.write(image,ConfigHelper.screenshotType,output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

}
