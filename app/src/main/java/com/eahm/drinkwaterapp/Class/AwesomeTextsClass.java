package com.eahm.drinkwaterapp.Class;

import com.eahm.drinkwaterapp.Utils.Utils;

import java.util.Calendar;

public class AwesomeTextsClass {

    int currentHourOfDay = 0;
    int currentMinute = 0;

    private void updateCurrentTime(){
        Calendar calendar = Calendar.getInstance();
        currentHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);
    }

    //region GET NICE TEXTS
    public String getCoutdownTextDone(){

        String message = "";
        //switch random text here!

        int randomPos1 = new Utils().getRandomNumber(1, 15);   //This gives a random integer between 7 (inclusive) and 0 (exclusive).
        switch (randomPos1){
            case 1:
                message = message + "\n" + "El agua ofrece vitalidad y salud, necesarias para realizar actividades fisicas y mantenerse motivado.";
                break;
            case 2:
                message = message + "\n" + "Una buena hidratación mejora el funcionamiento de nuestro cuerpo.";
                break;
            case 3:
                message = message + "\n" + "Tu cuerpo se sentira mucho mejor si lo mantienes hidratado.";
                break;
            case 4:
                message = message + "\n" + "El consumo de agua debe ser cuando tu cuerpo te lo pida, si no tienes sed entonces no debes tomar agua. Debes encontrar el balance correcto.";
                break;
            case 5:
                message = message + "\n" + "Un consumo regular de agua contribuye a un mejor funcionamiento de los riñones.";
                break;
            case 6:
                message = message + "\n" + "Recuerda tienes una meta que quieres cumplir";
                break;
            case 7:
                message = message + "\n" + "No es dificil, vamos a buscar un vaso de agua, un paso a la vez...";
                break;
            case 8:
                message = message + "\n" + "Si bebieses unos 10 vasos de agua fría al día, quemarías un extra de ¡490 calorías por semana";
                break;
            case 9:
                message = message + "\n" + "Desintoxicación del cuerpo, mejor tono muscular y piel más sana son algunos de los beneficios de ingerir regularmente H2O";
                break;
            case 10:
                message = message + "\n" + "Es preferible beber agua con las comidas antes que otros líquidos";
                break;
            case 11:
                message = message + "\n" + "Lleva una botella de agua pequeña a la oficina, a la escuela, etc. para no olvidar mantenerte hidratado";
                break;
            case 12:
                message = message + "\n" + "Si deseas.. puedes agregar limón al agua para darle sabor ademas ayuda a tomar más regularmente";
                break;
            case 13:
                message = message + "\n" + "Mentalizarse y no encontrar excusas para beber agua, recuerda que es un elemento esencial en el cuerpo y en el proceso de pérdida de peso. Luego de tomarla regularmente por 3 días se convierte en un hábito... o eso dicen...";
                break;
            default:
                    message = message + "\n" + "Tienes sed?";
                    break;
        }

        return message;
    }

    public String getTitle(int category) {
        //<>
        String title = "Hora de beber agua";
        //switch random text here!

        switch (category){
            case 1:
                //region TITULOS MAÑANEROS
                int randomPos1 = new Utils().getRandomNumber(1, 9);
                switch (randomPos1){
                    case 1:
                        title = "Buenos dias!";
                        break;
                    case 2:
                        title = "Buen dia!";
                        break;
                    case 3:
                        title = "Hora de comenzar";
                        break;
                    case 4:
                        title = "Nuevo jornada empieza";
                        break;
                    case 5:
                        title = "Dormiste bien?";
                        break;
                    case 6:
                        title = "Hora de empezar!";
                        break;
                    case 7:
                        title = "Hora de iniciar!";
                        break;
                    default:
                        title = "Hora de H20!";
                        break;
                }
                //endregion TITULOS MAÑANEROS
                break;
            case 2:
                //region TITULOS PARA LAST ALARM
                int randomPos2 = new Utils().getRandomNumber(1, 5);
                switch (randomPos2){
                    case 1:
                        title = "Hora de terminar";
                        break;
                    case 2:
                        title = "Terminamos... por hoy";
                        break;
                    case 3:
                        title = "Tenemos el resumen de la jornada";
                        break;
                    default:
                        title = "Reporte de la jornada!";
                        break;
                }
                //endregion TITULOS PARA LAST ALARM
                break;
            case 3:
                //region TITULOS DE LA ALARMA
                int randomPos3 = new Utils().getRandomNumber(1, 5);
                switch (randomPos3){
                    case 1:
                        title = "Llego la hora";
                        break;
                    case 2:
                        title = "Tienes sed?";
                        break;
                    case 3:
                        title = "Toca hidratarte";
                        break;
                    default:
                        title = "Beber agua";
                        break;
                }
                //endregion TITULOS DE LA ALARMA
                break;
        }

        return title;

    }

    public String getText(int category){
        //<>
        String contentText = "Listo para tomar tu primer vaso de agua?";

        switch (category){
            case 1:
                //region TEXTO MAÑANEROS
                int randomPos1 = new Utils().getRandomNumber(1, 5);
                switch (randomPos1){
                    case 1:
                        String amount = "";
                        float goalValue = new SettingsClass().getGoalValue();
                        if(goalValue % 1 == 0){
                            long integer = (long) goalValue;
                            amount = integer + "mL";
                        }
                        else amount = goalValue + "mL";

                        if(amount.isEmpty()){
                            contentText = "Empecemos para alcanzar la meta de hoy!";
                        }
                        else contentText = "Empecemos con nuestra meta de " + amount + " de agua para hoy!";
                        break;
                    case 2:
                        contentText = "Recuerda tomar agua cuando realmente tengas sed";
                        break;
                    case 3:
                        contentText = "Pierde la pereza y empieza hidratado tú dia!";
                        break;
                    default:
                        contentText = "Hora de levantarse con el pie derecho";
                        break;
                }
                //endregion TEXTO MAÑANEROS
                break;
            case 2:
                //region TEXTO PARA LAST ALARM
                contentText = "";
                int randomPos2 = new Utils().getRandomNumber(1, 4);
                switch (randomPos2){
                    case 1:
                        contentText += "Consumiste " + new Utils().getFinalProgressString(false) + ". Entra para ver mas detalles";
                        break;
                    case 2:
                        contentText += "Tomaste " + new Utils().getFinalProgressString(true) + " de agua durante esta jornada";
                        break;
                    default:
                        contentText += "Quieres saber a que horas ingeriste agua hoy? Entra y revisa tu resumen";
                        break;
                }
                //endregion TEXTO PARA LAST ALARM
                break;
            case 3:
                //region TEXTO DE LA ALARMA
                int randomPos3 = new Utils().getRandomNumber(1, 5);
                switch (randomPos3){
                    case 1:
                        contentText = "Llego la hora H2O";
                        break;
                    case 2:
                        contentText = "Hey, ya tienes sed?";
                        break;
                    case 3:
                        contentText = "Toca hidratarte";
                        break;
                    default:
                        contentText = "Ingerir agua solo cuando tengas sed";
                        break;
                }
                //endregion TEXTO DE LA ALARMA
                break;
        }

        return contentText;
    }

    public String getBigText(int category){
        //<>
        updateCurrentTime();

        String bigText =  "Si no quieres tomar ahora te recordare en unos minutos mas tarde...";

        switch (category){
            case 1:
                //region TEXTO MAÑANEROS
                int randomPos1 = new Utils().getRandomNumber(1, 9);
                switch (randomPos1){
                    case 1:
                        bigText = "Esta temprano, toma agua con calma. Evita problemas al tragar, tu cuerpo esta apenas iniciando";
                        break;
                    case 2:
                        bigText = "Te recordamos tomar agua cuando tengas sed, puedes esperar un poco si aun no quieres levantarte de la cama";
                        break;
                    case 3:
                        bigText = "Espero hayas dormido bien! Te aseguro que hidratarte ahora te ayudara a tomar energias para empezar el dia";
                        break;
                    case 4:
                        bigText = "Posiblemente estas comodo en la cama... o talvez vas camino al trabajo... toma agua cuando puedas, puedes omitir tu primer vaso si aun no estas listo";
                        break;
                    case 5:
                        bigText = "Beber 1 o 2 vasos de agua en ayunas es ideal para la eliminación de las toxinas y todas las sustancias que nuestro cuerpo no necesita.";
                        break;
                    case 6:
                        bigText = "Un vaso de agua o más en ayunas ayuda a minimizar los efectos nocivos de sustancias como el alcohol, el tabaco, la contaminación, la comida chatarra, entre otros.";
                        break;
                    case 7:
                        bigText = "Hey, para combatir y prevenir el estreñimiento puedes empezar con H2O en las mañanas ya que estimula el movimiento intestinal, pero si es el primer vaso hazlo con calma";
                        break;
                    default:
                        bigText = "Durante la noche el organismo trabaja  reparando células y haciendo limpieza interna, por esta razón, beber un vaso de agua en ayunas es muy bueno ya que esto le dará un empujón al organismo para que libere todos los desechos que no necesita, a través de la orina";
                        break;
                }
                //endregion TEXTO MAÑANEROS
                break;
            case 2:
                //region TEXTO PARA LAST ALARM
                String today = new Utils().getTodayString();
                bigText = today + "\n";
                if(new SettingsClass().isGoalReached()){
                    int randomPos2 = new Utils().getRandomNumber(1, 5);
                    switch (randomPos2){
                        case 1:
                            bigText += "Has consumido " + new Utils().getFinalProgressString(false) + "\nRecuerda que es importante mantener el ritmo, recuerda dar un vistazo a tu actividad presionando en esta notificacion";
                            break;
                        case 2:
                            bigText += new Utils().getFinalProgressString(false) + " bien hecho! Las notificaciones ya no apareceran en tu telefono, sera hasta la proxima hora de inicio";
                            break;
                        case 3:
                            bigText += "Tenemos tu resumen de hoy! Entra y revisa que tal fue el dia de hoy";
                            break;
                        default:
                            bigText += "Las horas de descanso empiezan ahora y tú has ingerido " + new Utils().getFinalProgressString(true) + "hasta este momento. Entra en esta notificacion para ver detalles";
                            break;
                    }
                }
                else {
                    int randomPos2 = new Utils().getRandomNumber(1, 5);
                    switch (randomPos2){
                        case 1:
                            bigText += "FELICIDADES\nLograste completar tu meta diaria con " + new Utils().getFinalProgressString(false) + " de agua ingeridos el dia de hoy";
                            break;
                        case 2:
                            bigText += "EXCELENTE!\nCon " + new Utils().getFinalProgressString(false) + " has logrado pasar la marca establecida. Excelente jornada!";
                            break;
                        case 3:
                            bigText += "HAS ALCANZADO LA META!\n Lo lograste, entra y mira tu actividad de hoy. SIGUE ASI!";
                            break;
                        default:
                            bigText += "LO LOGRASTE!\nSon " + new Utils().getFinalProgressString(true) + " que tu cuerpo ha añadido para funcionar correctamente\nMuchas Felicitaciones!\n\nEntra en esta notificacion para ver tu actividad";
                            break;
                    }
                }


                //endregion TEXTO PARA LAST ALARM
                break;
            case 3:
                //region TEXTO DE LA ALARMA
                int randomPos3 = new Utils().getRandomNumber(1, 8);
                switch (randomPos3){
                    case 1:
                        bigText = "Sabes que beber agua ayuda a aliviar la fatiga? " +
                                "El agua es utilizada por el cuerpo para eliminar toxinas y productos de desecho que este no necesita. " +
                                "Cuando hay menos agua en el cuerpo, el corazón tiene que trabajar más para bombear la sangre oxigenada a " +
                                "todas las células y otros órganos principales y eso puede causar la fatiga. Vamos por un vaso de agua!";
                        break;
                    case 2:
                        bigText= "Una piel sana? El agua ayuda a reponer los tejidos de la piel, así como a hidratarla y aumentar su elasticidad. " +
                                "Cuando el cuerpo recibe suficiente agua, la piel estará hidratada. También ayuda con las cicatrices, acné, arrugas " +
                                "y otros síntomas de envejecimiento";
                        break;
                    case 3:
                        bigText = "Algunos estudios indican que el consumo de agua también reduce el riesgo de cáncer de vejiga y colon. " +
                                "El agua destruye los agentes causantes de cáncer reduciendo el riesgo de diferentes tipos de cáncer.";
                        break;
                    case 4:
                        bigText = "El mal aliento es una clara señal de que el cuerpo necesita más agua. " +
                                "La saliva ayuda a tu boca a liberarse de bacterias y mantener la lengua hidratada. " +
                                "Por esto, cada vez que tengamos mal aliento, lo primero que debemos hacer es beber mucha agua";
                        break;
                    case 5:
                        bigText =  "El H2O ayuda a eliminar los subproductos de la grasa. " +
                                "Cuando bebemos el estómago se llena haciéndonos perder un poco el apetito. " +
                                "El agua no contiene calorías, grasa, carbohidratos ni azúcar y es un gran reemplazo de las bebidas con alto contenido calórico";
                        break;
                    case 6:
                        bigText =  "Ingerir agua eleva las defensas naturales de nuestro organismo y tiene un efecto lubricante en " +
                                "nuestros músculos y articulaciones, ya que las protege y ayuda a que funcionen correctamente. Así se mejora la resistencia de nuestros ligamentos y músculos.";
                        break;
                    default:
                        bigText = "El mal aliento es una clara señal de que el cuerpo necesita más agua. " +
                                "La saliva ayuda a tu boca a liberarse de bacterias y mantener la lengua hidratada. " +
                                "Por esto, cada vez que tengamos mal aliento, lo primero que debemos hacer es beber mucha agua";
                        break;
                }
                //endregion TEXTO DE LA ALARMA
                break;
        }

        /*if (currentHourOfDay >= 5 && currentHourOfDay <= 6){
            bigText = "Un vaso de agua tan temprano ayuda a refrescar y energizar tu cuerpo, bebelo con calma";
        }
        else if(currentHourOfDay > 6){
            bigText = "Recuerda un vaso ahora para lograr tu meta diaria";
        }*/

        return bigText;


    }

    //endregion GET NICE TEXTS
}
