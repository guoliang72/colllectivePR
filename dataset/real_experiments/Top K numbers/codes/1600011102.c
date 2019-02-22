#include<stdio.h>
#include<stdlib.h>
#include<string.h>
#include<math.h>


int main()
{
	int a[1002],i,j,k,n,temp;
	scanf("%d",&n);
	for(i=0;i<n;i=i+1) scanf("%d",&a[i]);
	for(i=0;i<n;i=i+1)
	{
		for(j=0;j<n-i;j=j+1)		// 边界错误
		{
			if(a[i]>a[j])			// 比较错误
			{
				temp=a[i];
				a[i]=a[j];
				a[j]=temp;
			}
		}
	}
	scanf("%d",&k);
	for(i=0;i<k;i=i+1) printf("%d\n",a[i]);
	return 0;
}