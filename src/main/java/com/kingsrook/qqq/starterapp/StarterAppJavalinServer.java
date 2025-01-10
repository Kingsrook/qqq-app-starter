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


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.session.QSystemUserSession;
import com.kingsrook.qqq.backend.core.scheduler.QScheduleManager;
import com.kingsrook.qqq.middleware.javalin.QApplicationJavalinServer;


/*******************************************************************************
 ** Start a javalin (http) qqq server.
 **
 ** Supported system properties:
 **
 ** -Dqqq.scheduleManager.enabled=false - do not start the ScheduleManager
 **   (used inside that class).
 **
 ** -Dqqq.javalin.hotSwapInstance=true - to cause the QInstance to be hot-swapped.
 **   Useful during development, to avoid needing as many server restarts.
 *******************************************************************************/
public class StarterAppJavalinServer
{
   private static final QLogger LOG = QLogger.getLogger(StarterAppJavalinServer.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void main(String[] args) throws QException
   {
      try
      {
         /////////////////////////////
         // define your application //
         /////////////////////////////
         StarterAppMetaDataProvider application = new StarterAppMetaDataProvider();

         /////////////////////////////////////
         // start the javalin (http) server //
         /////////////////////////////////////
         QApplicationJavalinServer javalinServer = new QApplicationJavalinServer(application);
         javalinServer.start();

         //////////////////////////////////////////////////////////////////////////
         // if you want to use scheduled jobs, start the schedule manager module //
         //////////////////////////////////////////////////////////////////////////
         QScheduleManager scheduleManager = QScheduleManager.initInstance(application.defineQInstance(), () -> new QSystemUserSession());
         scheduleManager.start();
      }
      catch(Exception e)
      {
         LOG.error("Error starting application server.", e);
      }
   }

}
