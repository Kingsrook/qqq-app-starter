/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.starterapp;


import java.util.HashMap;
import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QMetaDataVariableInterpreter;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleDispatcher;
import com.kingsrook.qqq.backend.core.modules.authentication.QAuthenticationModuleInterface;
import com.kingsrook.qqq.backend.core.scheduler.ScheduleManager;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.javalin.QJavalinImplementation;
import io.javalin.Javalin;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 ** Start a javalin (http) qqq server.
 **
 ** Supported environment variables:
 **   SYSTEM_USER_OAUTH_TOKEN - for working with an auth0 authentication module,
 **      this value is the bearer token used for system-sessions (e.g., scheduled jobs)
 **
 ** Supported system properties:
 **   -Dqqq.scheduleManager.enabled=false - do not start the ScheduleManager (used inside that class)
 **   -Dqqq.javalin.hotSwapInstance=true - to cause the QInstance to be hot-swapped - useful during development, to avoid many server restarts
 *******************************************************************************/
public class StarterAppJavalinServer
{
   private static final QLogger LOG = QLogger.getLogger(StarterAppJavalinServer.class);

   private static final int PORT = 8000;

   private static QInstance qInstance;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args) throws QInstanceValidationException
   {
      QInstance qInstance = StarterAppMetaDataProvider.defineInstance();
      new StarterAppJavalinServer(qInstance).startJavalinServer(PORT);

      ScheduleManager scheduleManager = ScheduleManager.getInstance(qInstance);
      scheduleManager.setSessionSupplier(StarterAppJavalinServer::getSystemSession);
      scheduleManager.start();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public StarterAppJavalinServer(QInstance qInstance)
   {
      StarterAppJavalinServer.qInstance = qInstance;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void startJavalinServer(int port) throws QInstanceValidationException
   {
      QJavalinImplementation qJavalinImplementation = new QJavalinImplementation(qInstance);

      Javalin service = Javalin.create(config ->
      {
         ////////////////////////////////////////////////////////////////////////////////////////
         // If you have any assets to add to the web server (e.g., logos, icons) place them at //
         // src/main/resources/material-dashboard-overlay (or a directory of your choice       //
         // under src/main/resources) and use this line of code to tell javalin about it.      //
         // Make sure to add your app-specific directory to the javalin config before the core //
         // material-dashboard directory, so in case the same file exists in both (e.g.,       //
         // favicon.png), the app-specific one will be used.                                   //
         ////////////////////////////////////////////////////////////////////////////////////////
         config.staticFiles.add("/material-dashboard-overlay");

         /////////////////////////////////////////////////////////////////////
         // tell javalin where to find material-dashboard static web assets //
         /////////////////////////////////////////////////////////////////////
         config.staticFiles.add("/material-dashboard");

         ////////////////////////////////////////////////////////////
         // set the index page for the SPA from material dashboard //
         ////////////////////////////////////////////////////////////
         config.spaRoot.addFile("/", "material-dashboard/index.html");
      }).start(port);

      ///////////////////////////////////////////
      // add qqq routes to the javalin service //
      ///////////////////////////////////////////
      service.routes(qJavalinImplementation.getRoutes());

      //////////////////////////////////////////////////////////////////////////////////////
      // per system property, set the server to hot-swap the q instance before all routes //
      //////////////////////////////////////////////////////////////////////////////////////
      String hotSwapPropertyValue = System.getProperty("qqq.javalin.hotSwapInstance", "false");
      if(BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(hotSwapPropertyValue)))
      {
         QJavalinImplementation.setQInstanceHotSwapSupplier(StarterAppMetaDataProvider::defineInstance);
         service.before(QJavalinImplementation::hotSwapQInstance);
      }
   }



   /*******************************************************************************
    ** This function serves as the session-supplier for scheduled jobs.
    *******************************************************************************/
   public static QSession getSystemSession()
   {
      try
      {
         QAuthenticationModuleDispatcher qAuthenticationModuleDispatcher = new QAuthenticationModuleDispatcher();
         QAuthenticationModuleInterface  authenticationModule            = qAuthenticationModuleDispatcher.getQModule(qInstance.getAuthentication());
         Map<String, String>             authenticationContext           = new HashMap<>();

         // todo fix this
         String token = new QMetaDataVariableInterpreter().interpret("${env.SYSTEM_USER_OAUTH_TOKEN}");
         authenticationContext.put("sessionId", token);

         return (authenticationModule.createSession(qInstance, authenticationContext));
      }
      catch(Exception e)
      {
         // todo!! LOG.error("Error creating system session", e);
         return (null);
      }
   }

}
