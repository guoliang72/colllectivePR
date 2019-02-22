#include <stdio.h>

int main(){
float i;
    int n;
int a;
int sum;
	sum=0;
scanf("%f",&n);
for(i=0;i<n;i=i+3){
        scanf("%lf",&a);
        if(a<100){
			a=0;
		}else if(a>=100 && a<200){
a=0.3*a;
		}else if(a>=200 && a<500){
			a=0.3*a;
		}else if(a>=500){
			a=0.5*a;
		}
        sum=sum+a;
    }
    printf("%.2lf\n",sum);
	return 0;
}

