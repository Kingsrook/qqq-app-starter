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
         // under src/main/resources) and add them to the javalin config here.                 //
         // Make sure to put app-specific directory first -- in case the same file exists in   //
         // both (e.g., favicon.png), so the app-specific one will be returned.                //
         ////////////////////////////////////////////////////////////////////////////////////////
         // config.staticFiles.add("/material-dashboard-overlay");

         config.staticFiles.add("/material-dashboard");
         config.spaRoot.addFile("/", "material-dashboard/index.html");
      }).start(port);

      service.routes(qJavalinImplementation.getRoutes());

      //////////////////////////////////////////////////////////////////////////////
      // per env var, set the server to hot-swap the q instance before all routes //
      //////////////////////////////////////////////////////////////////////////////
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      String                       hotSwap     = interpreter.interpret("${env.QQQ_HOT_SWAP_INSTANCE}");
      if(BooleanUtils.isTrue(ValueUtils.getValueAsBoolean(hotSwap)))
      {
         QJavalinImplementation.setQInstanceHotSwapSupplier(StarterAppMetaDataProvider::defineInstance);
         service.before(QJavalinImplementation::hotSwapQInstance);
      }
   }

}
